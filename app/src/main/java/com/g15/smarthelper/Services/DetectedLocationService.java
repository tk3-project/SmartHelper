package com.g15.smarthelper.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.g15.smarthelper.Constants;
import com.g15.smarthelper.MainActivity;
import com.g15.smarthelper.R;
import com.g15.smarthelper.receiver.LocationUpdateReceiver;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;


/**
 * The {@link DetectedLocationService} handles the activation and deactivation of the location
 * tracking. The service starts as a foreground service when the app is closed to obtain location
 * updates in the background.
 */
public class DetectedLocationService extends Service {

    private static final String LOG_TAG = "DetectedLocationService";
    private static final String CHANNEL_ID = "foreground-service-notifications";
    private static final int NOTIFICATION_ID = 9213875;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private boolean isActive = false;

    private final IBinder locBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public DetectedLocationService getService() {
            return DetectedLocationService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "Detected Location Service created.");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        createLocationRequest();
        fetchLocation();

        HandlerThread handlerThread = new HandlerThread(LOG_TAG);
        handlerThread.start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.v(LOG_TAG, "Creating notification channel for foreground service notification.");
            CharSequence name = getString(R.string.app_name);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Fetch the latest device location.
     */
    private void fetchLocation() {
        try {
            Log.v(LOG_TAG, "Fetching latest location.");
            fusedLocationClient.getLastLocation();
        } catch (SecurityException ex) {
            Log.e(LOG_TAG, "Lost location permission.", ex);
        }
    }

    /**
     * Create a location request that determines the QoS of the location updates.
     */
    private void createLocationRequest() {
        Log.v(LOG_TAG, "Creating location request.");
        locationRequest = new LocationRequest()
                .setInterval(Constants.UPDATE_INTERVAL)
                .setFastestInterval(Constants.FASTEST_UPDATE_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(Constants.MAX_WAIT_TIME);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "Bound to DetectedLocationService");
        stopForeground(true);
        return locBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.v(LOG_TAG, "Re-Bound to DetectedLocationService");
        stopForeground(true);
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "Unbound from DetectedLocationService");
        if (isActive) {
            startForeground(NOTIFICATION_ID, createForegroundNotification());
        }
        return true;
    }

    private PendingIntent getBroadcastPendingIntent() {
        Intent intent = new Intent(this, LocationUpdateReceiver.class);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    /**
     * Start the activity recognition by registering the {@link LocationUpdateReceiver} for receiving location updates.
     */
    public void startTracking() {
        Log.i(LOG_TAG, "Starting location tracking.");
        startService(new Intent(getApplicationContext(), DetectedLocationService.class));
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, getBroadcastPendingIntent())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Log.d(LOG_TAG, "Successfully requested location updates.");
                            Toast.makeText(getApplicationContext(),
                                    "Successfully requested location updates",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(LOG_TAG, "Failed to request location updates.", e);
                            Toast.makeText(getApplicationContext(),
                                    "Requesting location updates failed to start",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
            isActive = true;
        } catch (SecurityException ex) {
            Log.e(LOG_TAG, "Location permission denied.", ex);
        }
    }

    /**
     * Stop the activity recognition by unregistering the {@link LocationUpdateReceiver}.
     */
    public void stopTracking() {
        Log.i(LOG_TAG, "Stopping location tracking.");
        isActive = false;
        fusedLocationClient.removeLocationUpdates(getBroadcastPendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.d(LOG_TAG, "Successfully removed location updates.");
                        Toast.makeText(getApplicationContext(),
                                "Successfully removed location updates",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_TAG, "Failed to remove location  updates.", e);
                        Toast.makeText(getApplicationContext(),
                                "Removing location updates failed to start",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    /**
     * Create a fixed notification to allow the creation of a foreground service. The foreground
     * service is necessary to get reliable location updates in the background.
     * The created notification is displayed to the user while obtaining location updates.
     * @return The notification to inform the user about the usage of the location in the background.
     */
    private Notification createForegroundNotification () {
        Log.v(LOG_TAG, "Creating foreground notification.");
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentText(getString(R.string.foreground_service_msg))
                .setContentTitle(getString(R.string.foreground_service_title))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }

        return builder.build();
    }
}

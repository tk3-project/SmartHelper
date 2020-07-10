package com.g15.smarthelper.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.g15.smarthelper.Constants;
import com.g15.smarthelper.R;
import com.g15.smarthelper.Scenarios;
import com.g15.smarthelper.receiver.LocationUpdateReceiver;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import static com.g15.smarthelper.Scenarios.SHARED_PREFERENCES_KEY;

public class DetectedLocationService extends Service {

    private static final String LOG_TAG = "DetectedLocationService";
    private static final String CHANNEL_ID = "foreground-service-notifications";
    private static final int NOTIFICATION_ID = 9213875;

    private final IBinder serviceBinder = new IdentityBinder();

    public class IdentityBinder extends Binder {
        public DetectedLocationService getService() { return DetectedLocationService.this; }
    }

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean isActive = false;

    @Override
    public void onCreate() {

        Log.i(LOG_TAG, "Location service created");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        final Scenarios scenarios = new Scenarios(sharedPreferences);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.i(LOG_TAG, "Foreground location service fetched new location.");
                List<Location> locations = locationResult.getLocations();
                for (Location location : locations) {
                    LocationUpdateReceiver.processLocationUpdate(
                            getApplicationContext(), scenarios, location);
                }
            }
        };

        createLocationRequest();
        fetchLocation();

        HandlerThread handlerThread = new HandlerThread(LOG_TAG);
        handlerThread.start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void fetchLocation() {
        try {
            Log.v(LOG_TAG, "Fetching latest location.");
            fusedLocationClient.getLastLocation();
        } catch (SecurityException ex) {
            Log.e(LOG_TAG, "Lost location permission.", ex);
        }
    }

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
        return serviceBinder;
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

    public void startTracking() {
        Log.i(LOG_TAG, "Starting location tracking.");
        startService(new Intent(getApplicationContext(), DetectedLocationService.class));
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.myLooper());
            isActive = true;
        } catch (SecurityException ex) {
            Log.e(LOG_TAG, "Location permission denied.", ex);
        }
    }

    public void stopTracking() {
        Log.i(LOG_TAG, "Stopping location tracking.");
        isActive = false;
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private Notification createForegroundNotification () {
        Log.v(LOG_TAG, "Creating foreground notification.");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentText(getString(R.string.foreground_service_msg))
                .setContentTitle(getString(R.string.foreground_service_title))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }

        return builder.build();
    }
}

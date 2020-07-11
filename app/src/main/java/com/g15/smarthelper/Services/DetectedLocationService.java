package com.g15.smarthelper.Services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.g15.smarthelper.Constants;
import com.g15.smarthelper.R;
import com.g15.smarthelper.Scenarios;
import com.g15.smarthelper.receiver.LocationUpdateReceiver;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.concurrent.Executor;

import static com.g15.smarthelper.Scenarios.SHARED_PREFERENCES_KEY;

public class DetectedLocationService extends Service {

    private static final String LOG_TAG = "DetectedLocationService";
    private static final int NOTIFICATION_ID = 9213875;

    private Intent nIntentService;
    private PendingIntent nPendingIntent;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;

    private final IBinder locBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public DetectedLocationService getService() {
            return DetectedLocationService.this;
        }
    }

    @Override
    public void onCreate() {

        Log.i(LOG_TAG, "Location service created");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        nIntentService = new Intent(this, FetchAddressIntentService.class);
        nPendingIntent = PendingIntent.getService(this, 1, nIntentService, PendingIntent.FLAG_UPDATE_CURRENT);

        createLocationRequest();

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        final Scenarios scenarios = new Scenarios(sharedPreferences);

        /*locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.i(LOG_TAG, "Foreground location service fetched new location.");
                List<Location> detectedLocations = locationResult.getLocations();
                for (Location location : detectedLocations) {
                    Log.i(LOG_TAG, "Detected locations: " + location);
                    broadcastLocation(location);
                    LocationUpdateReceiver.processLocationUpdate(
                    getApplicationContext(), scenarios, location);
                }
            }
        };*/

        fetchLocation();

        HandlerThread handlerThread = new HandlerThread(LOG_TAG);
        handlerThread.start();
    }

    private void fetchLocation() {
        /*try {
            Log.v(LOG_TAG, "Fetching latest location.");
            fusedLocationClient.getLastLocation();
        } catch (SecurityException ex) {
            Log.e(LOG_TAG, "Lost location permission.", ex);
        }*/
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            //lLat = location.getLatitude();
                            //lLong = location.getLongitude();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_TAG, "Lost location permission." + e);
                    }
                });
    }

    private void createLocationRequest() {
        Log.v(LOG_TAG, "Creating location request.");
        locationRequest = new LocationRequest()
                .setInterval(Constants.UPDATE_INTERVAL)
                .setFastestInterval(Constants.FASTEST_UPDATE_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
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
        //stopForeground(true);
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "Unbound from DetectedLocationService");
        //if (isActive) {
        //startForeground(NOTIFICATION_ID, createForegroundNotification());
        //}
        return true;
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.setAction(Constants.ACTION_PROCESS_UPDATES);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    public void startTracking() {
        Log.i(LOG_TAG, "Starting location tracking.");
        startService(new Intent(getApplicationContext(), DetectedLocationService.class));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Void> task = fusedLocationClient.requestLocationUpdates(locationRequest, getPendingIntent());
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(getApplicationContext(),
                        "Successfully requested location updates",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),
                        "Requesting location updates failed to start",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    public void stopTracking() {
        Log.i(LOG_TAG, "Stopping location tracking.");
        fusedLocationClient.removeLocationUpdates(getPendingIntent());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTracking();
    }
}

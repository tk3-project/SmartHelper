package com.g15.smarthelper.Services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.g15.smarthelper.Constants;
import com.g15.smarthelper.receiver.ActivityUpdateReceiver;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;


/**
 * The {@link DetectedActivitiesService} handles the activation and deactivation of the activity
 * tracking.
 */
public class DetectedActivitiesService extends Service {

    private static final String LOG_TAG = "DetectedActivityService";

    private ActivityRecognitionClient mActivityRecognitionClient;

    private final IBinder actBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public DetectedActivitiesService getService() {
            return DetectedActivitiesService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.i(LOG_TAG, "DetectedActivitiesService created.");

        mActivityRecognitionClient = new ActivityRecognitionClient(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "Bound to DetectedActivitiesService.");
        return actBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private PendingIntent getBroadcastPendingIntent() {
        Intent intent = new Intent(this, ActivityUpdateReceiver.class);
        return PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Start the activity recognition by registering the {@link ActivityUpdateReceiver} for receiving activity updates.
     */
    public void startTracking() {
        Log.i(LOG_TAG, "Starting activity tracking.");

        mActivityRecognitionClient.requestActivityUpdates(Constants.DETECTION_INTERVAL_IN_MILLISECONDS, getBroadcastPendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.d(LOG_TAG, "Successfully started activity tracking.");
                        Toast.makeText(getApplicationContext(),
                                "Successfully requested activity updates",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_TAG, "Failed to start activity tracking.", e);
                        Toast.makeText(getApplicationContext(),
                                "Requesting activity updates failed to start",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    /**
     * Stop the activity recognition by unregistering the {@link ActivityUpdateReceiver}.
     */
    public void stopTracking() {
        Log.i(LOG_TAG, "Stopping activity tracking.");
        mActivityRecognitionClient.removeActivityUpdates(getBroadcastPendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.d(LOG_TAG, "Successfully stopped activity tracking.");
                        Toast.makeText(getApplicationContext(),
                                "Successfully removed activity updates",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(LOG_TAG, "Failed to stop activity tracking.", e);
                        Toast.makeText(getApplicationContext(),
                                "Removing activity updates failed to start",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }
}

package com.g15.smarthelper.Services;

import android.app.IntentService;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.g15.smarthelper.Constants;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

public class DetectedActivitiesIntentService extends IntentService{

    protected static final String LOG_TAG = DetectedActivitiesIntentService.class.getSimpleName();

    public DetectedActivitiesIntentService() {
        super(LOG_TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "Activity Intent Service created");
    }

    /** Define an onHandleIntent() method, which will be called
     * whenever an activity detection update is available
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void onHandleIntent(Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            Log.d(LOG_TAG, "Received a activity update intent.");
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

            for (DetectedActivity activity : detectedActivities) {
                Log.i(LOG_TAG, "Detected activity: " + activity.getType() + ", " + activity.getConfidence() + "%");
                broadcastActivity(activity);
            }
        }
    }

    private void broadcastActivity(DetectedActivity activity) {
        Intent intent = new Intent(Constants.BROADCAST_DETECTED_ACTIVITY);
        intent.putExtra("type", activity.getType());
        intent.putExtra("confidence", activity.getConfidence());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}


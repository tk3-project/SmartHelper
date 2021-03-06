package com.g15.smarthelper.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;


import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.g15.smarthelper.Constants;
import com.g15.smarthelper.ScenarioHandler.HomeAction;
import com.g15.smarthelper.ScenarioHandler.MusicAction;
import com.g15.smarthelper.ScenarioHandler.WarningAction;
import com.g15.smarthelper.Scenarios;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import java.util.ArrayList;
import java.util.Date;

import static com.g15.smarthelper.Constants.CONFIDENCE;
import static com.g15.smarthelper.Scenarios.SHARED_PREFERENCES_KEY;


/**
 * The {@link ActivityUpdateReceiver} is a {@link BroadcastReceiver} that handles updates of the
 * activity recognition api. New activity data is locally broadcasted and checked for matching
 * scenario conditions.
 */
public class ActivityUpdateReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "ActivityUpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && ActivityRecognitionResult.hasResult(intent)) {
            Log.i(LOG_TAG, "Received an activity update intent.");
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            if (result != null) {
                ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();
                for (DetectedActivity activity : detectedActivities) {
                    int type = activity.getType();
                    int confidence = activity.getConfidence();

                    if (confidence > CONFIDENCE) {
                        Log.i(LOG_TAG, "Received activity change " + type + " with confidence "
                                + confidence + ".");

                        SharedPreferences sharedPreferences = context.getSharedPreferences(
                                SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
                        Scenarios scenarios = new Scenarios(sharedPreferences);

                        broadcastActivity(activity, context);
                        processActivityUpdate(context, scenarios, type);
                    } else {
                        Log.i(LOG_TAG, "Received activity change " + type + " with confidence "
                                + confidence + " smaller than threshold " + CONFIDENCE + ".");
                    }
                }
            }
        }
    }

    /**
     * Broadcast a newly obtained activity update.
     * @param activity The activity to broadcast.
     * @param context The context to use for the broadcast.
     */
    private void broadcastActivity(DetectedActivity activity, Context context) {
        Intent intent = new Intent(Constants.BROADCAST_DETECTED_ACTIVITY);
        intent.putExtra("type", activity.getType());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        Log.i(LOG_TAG, "Locally broadcast activity update: " + activity);
    }

    /**
     * Process the activity update and trigger the scenarios if the conditions match.
     * @param context The context of the app.
     * @param scenarios The scenario utility to access the scenario states.
     * @param activityType The updated activity value.
     */
    private void processActivityUpdate(Context context, Scenarios scenarios, int activityType) {
        Scenarios.Scenario[] availableScenarios = Scenarios.Scenario.values();
        for (Scenarios.Scenario scenario : availableScenarios) {
            boolean isInFence = scenarios.getScenarioGeofenceEntered(scenario);
            boolean previouslyTriggered = scenarios.getScenarioTriggered(scenario);
            int previousActivity = scenarios.getCurrentActivity();

            int targetActivity = scenarios.getTargetActivity(scenario);

            if (activityType != previousActivity) {
                Log.d(LOG_TAG, "Activity changed from " + previousActivity + " to " + activityType + ".");

                boolean isInTimeFrame = scenarios.isInTimeFrame(scenario, new Date());

                if (isInFence && !previouslyTriggered && targetActivity == activityType && isInTimeFrame) {
                    Log.i(LOG_TAG, "Scenario " + scenario + " was triggered by activity " + activityType);
                    scenarios.setScenarioTriggered(scenario, true);
                    // Trigger scenario handler
                    if (scenario == Scenarios.Scenario.SCENARIO_MUSIC)
                        new MusicAction(context).sendNotification();
                    else if (scenario == Scenarios.Scenario.SCENARIO_WARNING)
                        new WarningAction(context).sendNotification();
                    else if (scenario == Scenarios.Scenario.SCENARIO_HOME)
                        new HomeAction(context).sendNotification();
                }
            }
        }
        scenarios.setCurrentActivity(activityType);
    }

}

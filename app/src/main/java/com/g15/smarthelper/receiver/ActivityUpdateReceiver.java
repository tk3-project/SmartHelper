package com.g15.smarthelper.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;


import com.g15.smarthelper.ScenarioHandler.WarningAction;
import com.g15.smarthelper.Scenarios;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import static com.g15.smarthelper.Constants.CONFIDENCE;
import static com.g15.smarthelper.Scenarios.SHARED_PREFERENCES_KEY;

public class ActivityUpdateReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "ActivityUpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && ActivityRecognitionResult.hasResult(intent)) {
            Log.d(LOG_TAG, "Received an activity update intent.");
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            if (result != null) {
                DetectedActivity activity = result.getMostProbableActivity();
                int confidence = result.getActivityConfidence(activity.getType());

                if (confidence > CONFIDENCE) {
                    Log.i(LOG_TAG, "Received activity change " + activity + " with confidence "
                            + confidence + ".");

                    SharedPreferences sharedPreferences = context.getSharedPreferences(
                            SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
                    Scenarios scenarios = new Scenarios(sharedPreferences);

                    processActivityUpdate(context, scenarios, activity.getType());
                } else {
                    Log.i(LOG_TAG, "Received activity change " + activity + " with confidence "
                            + confidence + " smaller than threshold " + CONFIDENCE + ".");
                }
            }
        }
    }

    private void processActivityUpdate(Context context, Scenarios scenarios, int activityType) {
        Scenarios.Scenario[] availableScenarios = Scenarios.Scenario.values();
        for (Scenarios.Scenario scenario : availableScenarios) {
            boolean isInFence = scenarios.getScenarioGeofenceEntered(scenario);
            boolean previouslyTriggered = scenarios.getScenarioTriggered(scenario);
            int previousActivity = scenarios.getCurrentActivity();

            int targetActivity = scenarios.getTargetActivity(scenario);

            if (activityType != previousActivity) {
                Log.d(LOG_TAG, "Activity changed from " + previousActivity + " to " + activityType + ".");

                if (isInFence && !previouslyTriggered && targetActivity == activityType) {
                    Log.i(LOG_TAG, "Scenario " + scenario + " was triggered by activity " + activityType);
                    scenarios.setScenarioTriggered(scenario, true);
                    // TODO: Trigger scenario handler
                    WarningAction warningAction = new WarningAction(context);
                    warningAction.SendNotifications();
                }
            }
        }
        scenarios.setCurrentActivity(activityType);
    }

}

package com.g15.smarthelper.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.g15.smarthelper.ScenarioHandler.WarningAction;
import com.g15.smarthelper.Scenarios;
import com.google.android.gms.location.LocationResult;

import java.util.List;

import static com.g15.smarthelper.Scenarios.SHARED_PREFERENCES_KEY;

public class LocationUpdateReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "LocationUpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && LocationResult.hasResult(intent)) {
            Log.d(LOG_TAG, "Received a location update intent.");
            LocationResult result = LocationResult.extractResult(intent);
            if (result != null) {
                List<Location> locations = result.getLocations();
                Location lastLocation = result.getLastLocation();
                Log.d(LOG_TAG, "Received last location: " + lastLocation);

                SharedPreferences sharedPreferences = context.getSharedPreferences(
                        SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
                Scenarios scenarios = new Scenarios(sharedPreferences);
                for (Location location : locations) {
                    processLocationUpdate(context, scenarios, location);
                }
            }
        }
    }

    private void processLocationUpdate(Context context, Scenarios scenarios, Location location) {
        Scenarios.Scenario[] availableScenarios = Scenarios.Scenario.values();
        for (Scenarios.Scenario scenario : availableScenarios) {
            boolean wasInGeofenceBefore = scenarios.getScenarioGeofenceEntered(scenario);
            Location fenceLocation = scenarios.getScenarioLocation(scenario);
            int radius = scenarios.getScenarioRadius(scenario);
            int currentActivity = scenarios.getCurrentActivity();

            float distance = location.distanceTo(fenceLocation);
            int targetActivity = scenarios.getTargetActivity(scenario);
            boolean isInFence = distance < radius;

            scenarios.setScenarioGeofenceEntered(scenario, isInFence);

            if (!wasInGeofenceBefore && isInFence && targetActivity == currentActivity) {
                Log.i(LOG_TAG, "Scenario " + scenario + " was triggered at location: " + location);
                scenarios.setScenarioTriggered(scenario, true);
                // TODO: Trigger scenario handler
                WarningAction warningAction = new WarningAction(context);
                warningAction.SendNotifications();
            } else if (wasInGeofenceBefore && !isInFence) {
                scenarios.setScenarioTriggered(scenario, false);
                Log.i(LOG_TAG, "Scenario " + scenario + " was left.");
            }
        }
    }
}

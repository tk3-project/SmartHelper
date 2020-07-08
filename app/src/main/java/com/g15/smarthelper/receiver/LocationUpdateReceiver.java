package com.g15.smarthelper.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

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
                    processLocationUpdate(scenarios, location);
                }
            }
        }
    }

    private void processLocationUpdate(Scenarios scenarios, Location location) {
        Scenarios.Scenario[] availableScenarios = Scenarios.Scenario.values();
        for (Scenarios.Scenario scenario : availableScenarios) {
            boolean wasTriggeringBefore = scenarios.getScenarioTriggered(scenario);
            Location fenceLocation = scenarios.getScenarioLocation(scenario);
            int radius = scenarios.getScenarioRadius(scenario);
            String currentActivity = scenarios.getCurrentActivity();

            float distance = location.distanceTo(fenceLocation);
            String targetActivity = scenarios.getTargetActivity(scenario);
            boolean isInFence = distance < radius;

            scenarios.setScenarioTriggered(scenario, isInFence);

            if (!wasTriggeringBefore && isInFence && targetActivity == currentActivity) {
                Log.i(LOG_TAG, "Scenario " + scenario + " was triggered at location: " + location);
                // TODO: Trigger scenario handler
            } else if (wasTriggeringBefore && !isInFence) {
                Log.i(LOG_TAG, "Scenario " + scenario + " was left.");
            }
        }
    }
}

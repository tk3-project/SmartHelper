package com.g15.smarthelper.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.g15.smarthelper.Constants;
import com.g15.smarthelper.ScenarioHandler.HomeAction;
import com.g15.smarthelper.ScenarioHandler.MusicAction;
import com.g15.smarthelper.ScenarioHandler.WarningAction;
import com.g15.smarthelper.Scenarios;
import com.google.android.gms.location.LocationResult;

import java.util.Date;
import java.util.List;

import static com.g15.smarthelper.Scenarios.SHARED_PREFERENCES_KEY;


/**
 * The {@link Location} is a {@link BroadcastReceiver} that handles updates of the
 * location api. New location data is locally broadcasted and checked for matching
 * scenario conditions.
 */
public class LocationUpdateReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "LocationUpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && LocationResult.hasResult(intent)) {
            Log.i(LOG_TAG, "Received a location update intent.");
            LocationResult result = LocationResult.extractResult(intent);
            if (result != null) {
                List<Location> locations = result.getLocations();
                Location lastLocation = result.getLastLocation();
                Log.d(LOG_TAG, "Received last location: " + lastLocation);

                SharedPreferences sharedPreferences = context.getSharedPreferences(
                        SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
                Scenarios scenarios = new Scenarios(sharedPreferences);
                for (Location location : locations) {
                    broadcastLocation(location, context);
                    processLocationUpdate(context, scenarios, location);
                }
            }
        }
    }

    /**
     * Broadcast a newly obtained location update.
     * @param location The location to broadcast.
     * @param context The context to use for the broadcast.
     */
    private void broadcastLocation(Location location, Context context) {
        Intent intent = new Intent(Constants.BROADCAST_DETECTED_LOCATION);
        intent.putExtra("latitude", location.getLatitude());
        intent.putExtra("longitude", location.getLongitude());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        Log.i(LOG_TAG, "Locally broadcast location update: " + location);
    }

    /**
     * Process the location update and trigger the scenarios if the conditions match.
     * @param context The context of the app.
     * @param scenarios The scenario utility to access the scenario states.
     * @param location The updated location value.
     */
    private void processLocationUpdate(Context context, Scenarios scenarios, Location location) {
        Log.d(LOG_TAG, "Processing location updates");
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

            Date date = new Date(location.getTime());
            boolean isInTimeFrame = scenarios.isInTimeFrame(scenario, date);

            Log.v(LOG_TAG, "Scenario " + scenario + " is in time frame: " + isInTimeFrame);

            if (!wasInGeofenceBefore && isInFence && targetActivity == currentActivity && isInTimeFrame) {
                Log.i(LOG_TAG, "Scenario " + scenario + " was triggered at location: " + location);
                scenarios.setScenarioTriggered(scenario, true);
                // Trigger scenario handler
                if (scenario == Scenarios.Scenario.SCENARIO_MUSIC)
                    new MusicAction(context).sendNotification();
                else if (scenario == Scenarios.Scenario.SCENARIO_WARNING)
                    new WarningAction(context).sendNotification();
                else if (scenario == Scenarios.Scenario.SCENARIO_HOME)
                    new HomeAction(context).sendNotification();
            } else if (wasInGeofenceBefore && !isInFence) {
                scenarios.setScenarioTriggered(scenario, false);
                Log.i(LOG_TAG, "Scenario " + scenario + " was left.");
            }
        }
    }
}

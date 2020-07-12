package com.g15.smarthelper;

import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

/**
 * This class allows access to the activation state of the scenarios.
 * The activation states of the scenarios are stored in the shared preferences.
 */

public class Scenarios {

    public enum Scenario {
        SCENARIO_MUSIC,
        SCENARIO_WARNING,
        SCENARIO_HOME
    }

    private static final String LOG_TAG = "scenarios";
    public static final String SHARED_PREFERENCES_KEY = "scenarios-shared-preferences";
    private static final String CURRENT_ACTIVITY = "current_activity";
    private static final String SCENARIOS_INITIALIZED = "scenarios_initialized";
    private static final String SCENARIO_TRIGGERED_FORMAT = "scenario%1$s_triggered";
    private static final String SCENARIO_ACTIVATED_FORMAT = "scenario%1$s_activated";
    private static final String SCENARIO_GEOFENCE_ENTERED_FORMAT = "scenario%1$s_geofence_entered";
    private static final String SCENARIO_LOCATION_SET_FORMAT = "scenario%1$s_location_set";
    private static final String SCENARIO_RADIUS_FORMAT = "scenario%1$s_radius";
    private static final String SCENARIO_LAT_FORMAT = "scenario%1$s_lat";
    private static final String SCENARIO_LNG_FORMAT = "scenario%1$s_lng";

    private SharedPreferences sharedPreferences;

    public Scenarios(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }


    /**
     * Returns if the specified scenario is currently activated.
     * @param scenario The scenario to check.
     * @return Returns if the scenario is active.
     */
    public boolean isScenarioActivated(Scenario scenario) {
        String scenarioName = String.format(SCENARIO_ACTIVATED_FORMAT, scenario);
        Boolean isActive = sharedPreferences.getBoolean(scenarioName, false);
        Log.d(LOG_TAG, "Scenario " + scenario + (isActive ? " is" : " is not") + " activated.");
        return isActive;
    }

    /**
     * Sets the activation state of a scenario to active.
     * @param scenario The scenario to change.
     */
    public void enableScenario(Scenario scenario) {
        Log.i(LOG_TAG, "Enabling scenario " + scenario);
        String scenarioName = String.format(SCENARIO_ACTIVATED_FORMAT, scenario);
        sharedPreferences.edit()
                .putBoolean(scenarioName, true)
                .commit();
    }

    /**
     * Sets the activation state of the scenario to inactive.
     * @param scenario The scenario to change.
     */
    public void disableScenario(Scenario scenario) {
        Log.i(LOG_TAG, "Disabling scenario " + scenario);
        String scenarioName = String.format(SCENARIO_ACTIVATED_FORMAT, scenario);
        sharedPreferences.edit()
                .putBoolean(scenarioName, false)
                .commit();
    }

    /**
     * Changes the activation state of the scenario to the specified value.
     * @param scenario The scenario to change.
     * @param enabled If the scenario should be enabled or disabled.
     */
    public void setScenarioEnabled(Scenario scenario, boolean enabled) {
        if (enabled) {
            this.enableScenario(scenario);
        } else {
            this.disableScenario(scenario);
        }
    }

    /**
     * Checks if any scenario is currently enabled.
     * @return true if any scenario is enabled, otherwise false.
     */
    public boolean isAnyScenarioEnabled() {
        return isScenarioActivated(Scenario.SCENARIO_MUSIC)
                || isScenarioActivated(Scenario.SCENARIO_WARNING)
                || isScenarioActivated(Scenario.SCENARIO_HOME);
    }

    /**
     * Returns the activity that a scenario is triggering at.
     * @param scenario The selected scenario.
     * @return The trigger activity.
     */
    public int getTargetActivity(Scenario scenario) {
        switch (scenario) {
            case SCENARIO_HOME: return DetectedActivity.STILL;
            case SCENARIO_MUSIC: return DetectedActivity.RUNNING;
            case SCENARIO_WARNING: return DetectedActivity.STILL;
        }
        return DetectedActivity.UNKNOWN;
    }

    /**
     * Stores the activity as last detected activity.
     * @param activity The activity to store.
     */
    public void setCurrentActivity(int activity) {
        Log.d(LOG_TAG, "Updating current activity to: " + activity);
        sharedPreferences.edit()
                .putInt(CURRENT_ACTIVITY, activity)
                .commit();
    }

    /**
     * Returns the last detected activity.
     * @return The last detected activity.
     */
    public int getCurrentActivity() {
        return sharedPreferences.getInt(CURRENT_ACTIVITY, DetectedActivity.UNKNOWN);
    }

    /**
     * Sets if the selected scenario is currently triggered.
     * @param scenario The selected scenario.
     * @param triggered The current trigger state.
     */
    public void setScenarioTriggered(Scenario scenario, boolean triggered) {
        Log.v(LOG_TAG, "Scenario " + scenario + " triggered: " + triggered);
        String scenarioName = String.format(SCENARIO_TRIGGERED_FORMAT, scenario);
        sharedPreferences.edit()
                .putBoolean(scenarioName, triggered)
                .commit();
    }

    /**
     * Returns if the selected scenario is currently triggered.
     * @param scenario The selected scenario.
     * @return If the scenario is triggered.
     */
    public boolean getScenarioTriggered(Scenario scenario) {
        String scenarioName = String.format(SCENARIO_TRIGGERED_FORMAT, scenario);
        return sharedPreferences.getBoolean(scenarioName, false);
    }

    /**
     * Stores if the corresponding geofence to the scenario is currently entered.
     * @param scenario The selected scenario
     * @param entered If the geofence is currently entered.
     */
    public void setScenarioGeofenceEntered(Scenario scenario, boolean entered) {
        Log.v(LOG_TAG, "Scenario " + scenario + " geofence entered: " + entered);
        String scenarioName = String.format(SCENARIO_GEOFENCE_ENTERED_FORMAT, scenario);
        sharedPreferences.edit()
                .putBoolean(scenarioName, entered)
                .commit();
    }

    /**
     * Returns if the geofence corresponding to the scenario is currently entered.
     * @param scenario The selected scenario.
     * @return If the geofence is currently entered.
     */
    public boolean getScenarioGeofenceEntered(Scenario scenario) {
        String scenarioName = String.format(SCENARIO_GEOFENCE_ENTERED_FORMAT, scenario);
        return sharedPreferences.getBoolean(scenarioName, false);
    }

    /**
     * Set the location trigger for the scenario.
     * @param scenario The selected scenario.
     * @param latitude The latitude of the geofence's center
     * @param longitude The longitude of the geofence's center
     * @param radius The geofence's radius
     */
    public void setScenarioFence(Scenario scenario, double latitude, double longitude, int radius) {
        Log.i(LOG_TAG, "Changing scenario " + scenario + " location: lat=" + latitude
                + ", lng=" + longitude + "(r=" + radius + ")");
        String locationSetName = String.format(SCENARIO_LOCATION_SET_FORMAT, scenario);
        String radiusName = String.format(SCENARIO_RADIUS_FORMAT, scenario);
        String latName = String.format(SCENARIO_LAT_FORMAT, scenario);
        String lngName = String.format(SCENARIO_LNG_FORMAT, scenario);
        sharedPreferences.edit()
                .putLong(latName, Double.doubleToRawLongBits(latitude))
                .putLong(lngName, Double.doubleToRawLongBits(longitude))
                .putInt(radiusName, radius)
                .putBoolean(locationSetName, true)
                .commit();
    }

    /**
     * Returns the center of the scenario's geofence.
     * @param scenario The selected scenario.
     * @return The center location of the geofence.
     */
    public Location getScenarioLocation(Scenario scenario) {
        String locationSetName = String.format(SCENARIO_LOCATION_SET_FORMAT, scenario);
        String latName = String.format(SCENARIO_LAT_FORMAT, scenario);
        String lngName = String.format(SCENARIO_LNG_FORMAT, scenario);
        boolean locationSet = sharedPreferences.getBoolean(locationSetName, false);
        if (!locationSet) {
            return null;
        }
        double latitude = Double.longBitsToDouble(sharedPreferences.getLong(latName, 0));
        double longitude = Double.longBitsToDouble(sharedPreferences.getLong(lngName, 0));
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    /**
     * Return the radius of the geofence corresponding to the scenario.
     * @param scenario The selected scenario.
     * @return The radius of the geofence.
     */
    public int getScenarioRadius(Scenario scenario) {
        String locationSetName = String.format(SCENARIO_LOCATION_SET_FORMAT, scenario);
        String radiusName = String.format(SCENARIO_RADIUS_FORMAT, scenario);
        boolean locationSet = sharedPreferences.getBoolean(locationSetName, false);
        if (!locationSet) {
            return -1;
        } else {
            return sharedPreferences.getInt(radiusName, 0);
        }
    }

    public void initializeScenarios() {
        sharedPreferences.edit()
                .putBoolean(SCENARIOS_INITIALIZED, true)
                .commit();
    }

    public boolean getScenariosInitialized() {
        return sharedPreferences.getBoolean(SCENARIOS_INITIALIZED, false);
    }

}

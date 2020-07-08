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

    private static String LOG_TAG = "scenarios";
    public static String SHARED_PREFERENCES_KEY = "scenarios-shared-preferences";

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
        String scenarioName = "scenario" + scenario + "_activated";
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
        String scenarioName = "scenario" + scenario + "_activated";
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
        String scenarioName = "scenario" + scenario + "_activated";
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

    public int getTargetActivity(Scenario scenario) {
        switch (scenario) {
            case SCENARIO_HOME: return DetectedActivity.STILL;
            case SCENARIO_MUSIC: return DetectedActivity.RUNNING;
            case SCENARIO_WARNING: return DetectedActivity.STILL;
        }
        return DetectedActivity.UNKNOWN;
    }

    public void setCurrentActivity(int activity) {
        sharedPreferences.edit()
                .putInt("current_activity", activity)
                .commit();
    }

    public int getCurrentActivity() {
        return sharedPreferences.getInt("current_activity", DetectedActivity.UNKNOWN);
    }

    public void setScenarioTriggered(Scenario scenario, boolean triggered) {
        String scenarioName = "scenario" + scenario + "_triggered";
        sharedPreferences.edit()
                .putBoolean(scenarioName, triggered)
                .commit();
    }

    public boolean getScenarioTriggered(Scenario scenario) {
        String scenarioName = "scenario" + scenario + "_triggered";
        return sharedPreferences.getBoolean(scenarioName, false);
    }

    public void setScenarioFence(Scenario scenario, double latitude, double longitude, int radius) {
        Log.i(LOG_TAG, "Changing scenario " + scenario + " location: lat=" + latitude
                + ", lng=" + longitude + "(r=" + radius + ")");
        String scenarioName = "scenario" + scenario;
        sharedPreferences.edit()
                .putLong(scenarioName + "_lat", Double.doubleToRawLongBits(latitude))
                .putLong(scenarioName + "_lng", Double.doubleToRawLongBits(longitude))
                .putInt(scenarioName + "_radius", radius)
                .putBoolean(scenarioName + "_location_set", true)
                .commit();
    }

    public Location getScenarioLocation(Scenario scenario) {
        String scenarioName = "scenario" + scenario;
        boolean locationSet = sharedPreferences.getBoolean(scenarioName + "_location_set", false);
        if (!locationSet) {
            return null;
        }
        double latitude = Double.longBitsToDouble(sharedPreferences.getLong(scenarioName + "_lat", 0));
        double longitude = Double.longBitsToDouble(sharedPreferences.getLong(scenarioName + "_lng", 0));
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    public int getScenarioRadius(Scenario scenario) {
        String scenarioName = "scenario" + scenario;
        boolean locationSet = sharedPreferences.getBoolean(scenarioName + "_location_set", false);
        if (!locationSet) {
            return -1;
        } else {
            return sharedPreferences.getInt(scenarioName + "_radius", 0);
        }
    }

}

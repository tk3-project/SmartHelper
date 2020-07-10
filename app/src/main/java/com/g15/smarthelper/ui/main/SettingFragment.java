package com.g15.smarthelper.ui.main;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.g15.smarthelper.MainActivity;
import com.g15.smarthelper.R;
import com.g15.smarthelper.ScenarioHandler.WarningAction;
import com.g15.smarthelper.Scenarios;
import com.g15.smarthelper.Services.DetectedActivitiesService;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.g15.smarthelper.Constants;
import static com.g15.smarthelper.Scenarios.SHARED_PREFERENCES_KEY;


public class SettingFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    private static String LOG_TAG = "settings-fragment";

    private Switch musicSwitch;
    private Switch warningSwitch;
    private Switch homeSwitch;
    private Scenarios scenarios;
    private Scenarios.Scenario currentTargetScenario;
    private CompoundButton currentTargetSwitch;
    private List<String> missingPermissions = new ArrayList<>();
    private int permissionCounter = 0;

    WarningAction warningAction;
    BroadcastReceiver mReceiver;
    LocalBroadcastManager broadcastManager;
    IntentFilter intentFilter;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.setting_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        musicSwitch = view.findViewById(R.id.switch1);
        warningSwitch = view.findViewById(R.id.switch2);
        homeSwitch = view.findViewById(R.id.switch3);

        SharedPreferences sharedPreferences = getActivity()
                .getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        scenarios = new Scenarios(sharedPreferences);
        warningAction = new WarningAction(getContext());

        initializeScenarioActivated();

        musicSwitch.setOnCheckedChangeListener(this);
        warningSwitch.setOnCheckedChangeListener(this);
        homeSwitch.setOnCheckedChangeListener(this);

        setInitialLocation();
    }

    private void setInitialLocation() {
        double herrnGartenLat = 49.8775;
        double herrnGartenLng = 8.6525;
        int herrnGartenRadius = 150; // in meters
        scenarios.setScenarioFence(Scenarios.Scenario.SCENARIO_MUSIC, herrnGartenLat, herrnGartenLng, herrnGartenRadius);

        double homeLat = 49.8727;
        double homeLng = 8.6312;
        int homeRadius = 50; // in meters
        scenarios.setScenarioFence(Scenarios.Scenario.SCENARIO_HOME, homeLat, homeLng, homeRadius);

        double warningLat = 49.8521;
        double warningLng = 8.6463;
        int warningRadius = 50; // in meters
        scenarios.setScenarioFence(Scenarios.Scenario.SCENARIO_WARNING, warningLat, warningLng, warningRadius);
    }

    /**
     * Checks which of the necessary permissions are granted.
     * @return A list of permission identifiers that are not granted, but needed.
     */
    private String[] identifyNeededPermissions() {
        List<String> neededPermissions = new ArrayList<>();

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.ACTIVITY_RECOGNITION);
            }
        }

        return neededPermissions.toArray(new String[neededPermissions.size()]);
    }

    /**
     * Checks if all needed permissions are granted.
     * @return true if all permissions are granted, otherwise false.
     */
    private boolean checkPermissions() {
        final String[] neededPermissions = identifyNeededPermissions();

        return neededPermissions.length == 0;
    }

    /**
     * Displays a descriptive dialog that prompts the user to grant the necessary permissions.
     */
    private void showPermissionRequestDialog() {
        final String[] neededPermissions = identifyNeededPermissions();

        if (neededPermissions.length == 0) {
            Log.d(LOG_TAG, "All permissions already granted.");
        }

        Log.d(LOG_TAG, "Showing dialog to grant permissions.");
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.required_permissions_title))
                .setMessage(getString(R.string.required_permissions_message))
                .setNeutralButton(getString(R.string.required_permissions_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Snackbar.make(getView(), getString(R.string.permission_dialog_canceled), Snackbar.LENGTH_LONG)
                                .show();
                        Log.i(LOG_TAG, "The user canceled the grant permission process.");
                    }
                })
                .setPositiveButton(getString(R.string.required_permissions_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.i(LOG_TAG, "User starts grant permission process.");
                        missingPermissions = new ArrayList<>(Arrays.asList(neededPermissions));
                        grantMissingPermission();
                    }
                })
                .show();
    }

    /**
     * Request the user to grant one of the missing permissions.
     */
    private void grantMissingPermission() {
        if (missingPermissions.size() > 0) {
            String permission = missingPermissions.remove(0);
            Log.d(LOG_TAG, "Requesting missing permission: " + permission);
            requestPermissions(new String[] {permission}, ++permissionCounter);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean permissionGranted = grantResults != null && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if (!permissionGranted) {
            Snackbar.make(getView(), getString(R.string.permission_dialog_canceled), Snackbar.LENGTH_LONG)
                    .show();
            Log.w(LOG_TAG, "Necessary permission not granted: " + permissions[0]);
            missingPermissions = new ArrayList<>();
        } else if (missingPermissions.size() > 0) {
            // The permissions have to be requested individually to work on Android 10.
            String permission = missingPermissions.remove(0);
            Log.d(LOG_TAG, "Requesting missing permission: " + permission);
            requestPermissions(new String[] {permission}, ++permissionCounter);
        } else {
            Log.i(LOG_TAG, "All needed permissions granted.");
            if (currentTargetScenario != null) {
                Log.i(LOG_TAG, "Activating pending scenario.");
                boolean activatedBefore = scenarios.isAnyScenarioEnabled();
                setScenarioEnabled(currentTargetScenario, true);
                currentTargetSwitch.setChecked(true);

                if (!activatedBefore) {
                    // The first scenario has been activated. Enable location and activity tracking.
                    enableLocationAndActivityTracking();
                }
                currentTargetScenario = null;
            }
        }
    }

    /**
     * This method restores the state of the switches to match the stored activation state.
     */
    private void initializeScenarioActivated() {
        if (scenarios.isScenarioActivated(Scenarios.Scenario.SCENARIO_MUSIC)) {
            musicSwitch.setChecked(true);
        }
        if (scenarios.isScenarioActivated(Scenarios.Scenario.SCENARIO_WARNING)) {
            warningSwitch.setChecked(true);
        }
        if (scenarios.isScenarioActivated(Scenarios.Scenario.SCENARIO_HOME)) {
            homeSwitch.setChecked(true);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean scenarioActivated) {
        boolean activatedBefore = scenarios.isAnyScenarioEnabled();
        Scenarios.Scenario currentScenario;
        if (compoundButton == musicSwitch) {
            Log.i(LOG_TAG, "Music scenario state changed to: " + scenarioActivated);
            currentScenario = Scenarios.Scenario.SCENARIO_MUSIC;
            if (scenarioActivated){
                //TODO
            }
        } else if (compoundButton == warningSwitch) {
            Log.i(LOG_TAG, "Warning scenario state changed to: " + scenarioActivated);
            currentScenario = Scenarios.Scenario.SCENARIO_WARNING;
            if (scenarioActivated){
                //MonitorWarningCondition();
            }
        } else if (compoundButton == homeSwitch) {
            Log.i(LOG_TAG, "Home scenario state changed to: " + scenarioActivated);
            currentScenario = Scenarios.Scenario.SCENARIO_HOME;
            if (scenarioActivated){
                //TODO
            }
        } else {
            Log.w(LOG_TAG, "Invalid scenario change triggered.");
            return;
        }

        boolean hasPermissions = checkPermissions();
        Log.d(LOG_TAG, hasPermissions ? "All permissions granted." : "Additional permissions needed.");
        if (scenarioActivated && !hasPermissions) {
            currentTargetScenario = currentScenario;
            currentTargetSwitch = compoundButton;
            currentTargetSwitch.setChecked(false);
            showPermissionRequestDialog();
            return;
        }

        setScenarioEnabled(currentScenario, scenarioActivated);

        if (!scenarios.isAnyScenarioEnabled()) {
            // No scenario enabled. Disable location and activity tracking.
            disableLocationAndActivityTracking();
        } else if (!activatedBefore) {
            // The first scenario has been activated. Enable location and activity tracking.
            enableLocationAndActivityTracking();
        }
    }

    /**
     * Enable and initialize the tracking of location and activity of the user.
     */
    private void enableLocationAndActivityTracking() {
        Log.i(LOG_TAG, "Start activity and location tracking.");
        Intent intent = new Intent(getActivity(), DetectedActivitiesService.class);
        getContext().startService(intent);
        ((MainActivity) getActivity()).requestLocationUpdates();
        ((MainActivity) getActivity()).requestActivityUpdates();
    }

    /**
     * Disable the tracking of location and activity of the user.
     */
    private void disableLocationAndActivityTracking() {
        Log.i(LOG_TAG, "Stop activity and location tracking.");
        Intent intent = new Intent(getActivity(), DetectedActivitiesService.class);
        getContext().stopService(intent);
        ((MainActivity) getActivity()).removeLocationUpdates();
        ((MainActivity) getActivity()).removeActivityUpdates();
    }

    /**
     * This method changes the activation state of a scenario.
     * @param scenario The scenario to change.
     * @param scenarioActivated If the scenario should be enabled or disabled.
     */
    private void setScenarioEnabled(Scenarios.Scenario scenario, boolean scenarioActivated) {
        scenarios.setScenarioEnabled(scenario, scenarioActivated);
    }


    // Process warning scenario here
    private void MonitorWarningCondition(){

        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BROADCAST_DETECTED_ACTIVITY);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int type = intent.getIntExtra("type", -1);
                int confidence = intent.getIntExtra("confidence", 0);
                Log.i(LOG_TAG, "Broadcast: Activity received, Type = " + type + ", Confidence = " + confidence);
                if (type == DetectedActivity.ON_FOOT || type == DetectedActivity.WALKING){
                    if (confidence > Constants.CONFIDENCE) {
                        warningAction.SendNotifications();
                    }
                }
            }
        };

        broadcastManager.registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //broadcastManager.unregisterReceiver(mReceiver);
    }
}
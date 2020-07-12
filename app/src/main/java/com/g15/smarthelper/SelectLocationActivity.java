package com.g15.smarthelper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static com.g15.smarthelper.Scenarios.SHARED_PREFERENCES_KEY;

public class SelectLocationActivity extends AppCompatActivity {

    private static final String LOG_TAG = "SelectLocationActivity";

    Scenarios scenarios;

    EditText musicLatTf;
    EditText musicLngTf;
    EditText musicRadiusTf;

    EditText warningLatTf;
    EditText warningLngTf;
    EditText warningRadiusTf;

    EditText homeLatTf;
    EditText homeLngTf;
    EditText homeRadiusTf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.location_selection_title);

        musicLatTf = findViewById(R.id.music_lat_textfield);
        musicLngTf = findViewById(R.id.music_lng_textfield);
        musicRadiusTf = findViewById(R.id.music_radius_textfield);

        warningLatTf = findViewById(R.id.warning_lat_textfield);
        warningLngTf = findViewById(R.id.warning_lng_textfield);
        warningRadiusTf = findViewById(R.id.warning_radius_textfield);

        homeLatTf = findViewById(R.id.home_lat_textfield);
        homeLngTf = findViewById(R.id.home_lng_textfield);
        homeRadiusTf = findViewById(R.id.home_radius_textfield);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        scenarios = new Scenarios(sharedPreferences);

        initLocationSettings();

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG, "Trying to write the changed location settings.");
                updateLocationSettings();
            }
        });
    }

    private void initLocationSettings() {
        Log.v(LOG_TAG, "Load location settings and initialize text fields");
        Location musicLocation = scenarios.getScenarioLocation(Scenarios.Scenario.SCENARIO_MUSIC);
        int musicRadius = scenarios.getScenarioRadius(Scenarios.Scenario.SCENARIO_MUSIC);
        Location warningLocation = scenarios.getScenarioLocation(Scenarios.Scenario.SCENARIO_WARNING);
        int warningRadius = scenarios.getScenarioRadius(Scenarios.Scenario.SCENARIO_WARNING);
        Location homeLocation = scenarios.getScenarioLocation(Scenarios.Scenario.SCENARIO_HOME);
        int homeRadius = scenarios.getScenarioRadius(Scenarios.Scenario.SCENARIO_HOME);

        musicLatTf.setText(String.valueOf(musicLocation.getLatitude()));
        musicLngTf.setText(String.valueOf(musicLocation.getLongitude()));
        musicRadiusTf.setText(String.valueOf(musicRadius));

        warningLatTf.setText(String.valueOf(warningLocation.getLatitude()));
        warningLngTf.setText(String.valueOf(warningLocation.getLongitude()));
        warningRadiusTf.setText(String.valueOf(warningRadius));

        homeLatTf.setText(String.valueOf(homeLocation.getLatitude()));
        homeLngTf.setText(String.valueOf(homeLocation.getLongitude()));
        homeRadiusTf.setText(String.valueOf(homeRadius));
    }

    private void updateLocationSettings() {
        try {
            double musicLat = Double.parseDouble(musicLatTf.getText().toString());
            double musicLng = Double.parseDouble(musicLngTf.getText().toString());
            int musicRadius = Integer.parseInt(musicRadiusTf.getText().toString());

            double warningLat = Double.parseDouble(warningLatTf.getText().toString());
            double warningLng = Double.parseDouble(warningLngTf.getText().toString());
            int warningRadius = Integer.parseInt(warningRadiusTf.getText().toString());

            double homeLat = Double.parseDouble(homeLatTf.getText().toString());
            double homeLng = Double.parseDouble(homeLngTf.getText().toString());
            int homeRadius = Integer.parseInt(homeRadiusTf.getText().toString());

            scenarios.setScenarioFence(Scenarios.Scenario.SCENARIO_MUSIC, musicLat, musicLng, musicRadius);
            scenarios.setScenarioFence(Scenarios.Scenario.SCENARIO_WARNING, warningLat, warningLng, warningRadius);
            scenarios.setScenarioFence(Scenarios.Scenario.SCENARIO_HOME, homeLat, homeLng, homeRadius);

            Log.i(LOG_TAG, "Location settings successfully updated.");
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "Invalid location settings input.", e);
            Toast.makeText(getApplicationContext(),
                    getString(R.string.location_update_failed_toast),
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
package com.g15.smarthelper;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import com.g15.smarthelper.Services.DetectedActivitiesService;
import com.g15.smarthelper.Services.DetectedLocationService;
import com.g15.smarthelper.Services.FetchAddressIntentService;
import com.g15.smarthelper.receiver.ActivityUpdateReceiver;
import com.g15.smarthelper.receiver.LocationUpdateReceiver;
import com.g15.smarthelper.ui.main.SectionsPagerAdapter;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import static com.g15.smarthelper.Constants.DETECTION_INTERVAL_IN_MILLISECONDS;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private DetectedLocationService locationService;
    protected ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            locationService = ((DetectedLocationService.IdentityBinder)binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            locationService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews(){
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        bindService(new Intent(this, DetectedLocationService.class),
                serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if (locationService != null) {
            unbindService(serviceConnection);
        }
        super.onStop();
    }


    public void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Necessary permission to access location is not granted. Location updates cannot be processed.");
            return;
        }
        if (locationService != null) {
            locationService.startTracking();
        }
    }

    public void removeLocationUpdates() {
        if (locationService != null) {
            locationService.stopTracking();
        }
    }

    public void requestActivityUpdates() {
        ActivityRecognitionClient client = ActivityRecognition.getClient(this);
        client.requestActivityUpdates(DETECTION_INTERVAL_IN_MILLISECONDS, getActivityPendingIntent());
    }

    public void removeActivityUpdates() {
        ActivityRecognitionClient client = ActivityRecognition.getClient(this);
        client.removeActivityUpdates(getActivityPendingIntent());
    }

    private PendingIntent getActivityPendingIntent() {
        Intent intent = new Intent(this, ActivityUpdateReceiver.class);
        return PendingIntent.getBroadcast(
                this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    // --- Fetch Address ---

    /**
     * Gets the address for the last known location.
     */
    //@SuppressWarnings("MissingPermission")
    /*private void getAddress() {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            Log.w(TAG, "onSuccess:null");
                            return;
                        }

                        mLastLocation = location;

                        // Determine whether a Geocoder is available.
                        if (!Geocoder.isPresent()) {
                            showSnackbar(getString(R.string.no_geocoder_available));
                            return;
                        }

                        // If the user pressed the fetch address button before we had the location,
                        // this will be set to true indicating that we should kick off the intent
                        // service after fetching the location.
                        if (mAddressRequested) {
                            startIntentService();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getLastLocation:onFailure", e);
                    }
                });
    }*/

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    /*private void startIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
        Log.d(TAG, "started service");
    }*/

    /**
     * Return the current state of the permissions needed.
     */
    /*private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    Constants.REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });

        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }*/

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    /*private void showSnackbar(final String text) {
        View container = findViewById(android.R.id.content);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }*/

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    /*private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }*/

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    /*private class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         *  Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         *
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                showToast(getString(R.string.address_found));
            }

            // Reset. Enable the Fetch Address button and stop showing the progress bar.
            mAddressRequested = false;
        }
    }*/

    /**
     * Updates the address in the UI.
     */
    /*private void displayAddressOutput() {
        Log.d(TAG, "mAddressOutput: " + mAddressOutput);
    }*/

    /**
     * Shows a toast with the given text.
     */
    /*private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }*/

    /**
     * Updates fields based on data stored in the bundle.
     */
    /*private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Check savedInstanceState to see if the address was previously requested.
            if (savedInstanceState.keySet().contains(Constants.ADDRESS_REQUESTED_KEY)) {
                mAddressRequested = savedInstanceState.getBoolean(Constants.ADDRESS_REQUESTED_KEY);
            }
            // Check savedInstanceState to see if the location address string was previously found
            // and stored in the Bundle. If it was found, display the address string in the UI.
            if (savedInstanceState.keySet().contains(Constants.LOCATION_ADDRESS_KEY)) {
                mAddressOutput = savedInstanceState.getString(Constants.LOCATION_ADDRESS_KEY);
                displayAddressOutput();
            }
        }
    }*/
}
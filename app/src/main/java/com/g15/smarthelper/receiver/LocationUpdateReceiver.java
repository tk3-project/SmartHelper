package com.g15.smarthelper.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import java.util.List;

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
                Log.i(LOG_TAG, "Received last location: " + lastLocation);

                // TODO: Process all locations and trigger scenarios here
            }
        }
    }
}

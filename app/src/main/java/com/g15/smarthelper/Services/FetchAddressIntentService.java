/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.g15.smarthelper.Services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.g15.smarthelper.Constants;
import com.google.android.gms.location.LocationResult;

import java.util.List;

/**
 * Asynchronously handles an intent using a worker thread. Receives a ResultReceiver object and a
 * location through an intent. Tries to fetch the address for the location using a Geocoder, and
 * sends the result to the ResultReceiver.
 */
public class FetchAddressIntentService extends IntentService {
    protected static final String LOG_TAG = FetchAddressIntentService.class.getSimpleName();

    public FetchAddressIntentService() {
        super(LOG_TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "Location Intent Service created");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (LocationResult.hasResult(intent)) {
            Log.d(LOG_TAG, "Received a location update intent.");
            LocationResult result = LocationResult.extractResult(intent);
            if (result != null) {
                List<Location> detectedLocations = result.getLocations();
                Location lastLocation = result.getLastLocation();
                Log.d(LOG_TAG, "Received last location: " + lastLocation);

                for (Location location : detectedLocations) {
                    Log.i(LOG_TAG, "Detected locations: " + location);
                    broadcastLocation(location);
                }
            }
        }
    }

    private void broadcastLocation(Location location) {
        Intent intent = new Intent(Constants.BROADCAST_DETECTED_LOCATION);
        intent.putExtra("latitude", location.getLatitude());
        intent.putExtra("longtitude", location.getLongitude());
        intent.putExtra("time", location.getTime());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.i(LOG_TAG, "Location update already sent");
    }
}
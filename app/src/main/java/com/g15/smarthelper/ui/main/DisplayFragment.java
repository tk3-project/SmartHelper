package com.g15.smarthelper.ui.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.g15.smarthelper.R;
import com.g15.smarthelper.Constants;

import com.g15.smarthelper.receiver.ActivityUpdateReceiver;
import com.g15.smarthelper.receiver.LocationUpdateReceiver;

import com.google.android.gms.location.DetectedActivity;


public class DisplayFragment extends Fragment {

    private static String LOG_TAG = "display-fragment";
    ActivityUpdateReceiver activityReceiver;
    LocationUpdateReceiver locationReceiver;

    private TextView txtActivity, txtLocation;
    private ImageView imgActivity;
    private String default_loc, default_act;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.display_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        default_loc = getString(R.string.location_unknown);
        default_act = getString(R.string.activity_unknown);

        txtActivity = getActivity().findViewById(R.id.txt_activity);
        txtLocation = getActivity().findViewById(R.id.txt_location);
        imgActivity = getActivity().findViewById(R.id.img_activity);

        txtLocation.setText("Location: " + default_loc);
        txtActivity.setText("Activity: " + default_act);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activityReceiver = new ActivityUpdateReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int type = intent.getIntExtra("type", -1);
                Log.i(LOG_TAG, "Broadcast: Activity received, Type = " + type);
                handleUserActivity(type);
            }
        };

        locationReceiver = new LocationUpdateReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double latitude = intent.getDoubleExtra("latitude", 0);
                double longtitude = intent.getDoubleExtra("longtitude", 0);
                Log.i(LOG_TAG, "Broadcast: Location received, latitude = " + latitude + "; longtitude = " + longtitude);
                handleUserLocation(latitude, longtitude);
            }
        };
    }
    private void handleUserLocation(double Lat, double Long){
        String location = "Lat: " + Lat + "; Long: " + Long;
        txtLocation.setText(location);
    }

    private void handleUserActivity(int type) {
        String label = getString(R.string.activity_unknown);
        int icon = R.drawable.ic_still;

        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                label = getString(R.string.activity_in_vehicle);
                icon = R.drawable.ic_driving;
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                label = getString(R.string.activity_on_bicycle);
                icon = R.drawable.ic_on_bicycle;
                break;
            }
            case DetectedActivity.ON_FOOT: {
                label = getString(R.string.activity_on_foot);
                icon = R.drawable.ic_walking;
                break;
            }
            case DetectedActivity.RUNNING: {
                label = getString(R.string.activity_running);
                icon = R.drawable.ic_running;
                break;
            }
            case DetectedActivity.STILL: {
                label = getString(R.string.activity_still);
                break;
            }
            case DetectedActivity.TILTING: {
                label = getString(R.string.activity_tilting);
                icon = R.drawable.ic_tilting;
                break;
            }
            case DetectedActivity.WALKING: {
                label = getString(R.string.activity_walking);
                icon = R.drawable.ic_walking;
                break;
            }
            case DetectedActivity.UNKNOWN: {
                label = getString(R.string.activity_unknown);
                break;
            }
        }

        Log.e(LOG_TAG, "User activity: " + label);

        txtActivity.setText(label);
        imgActivity.setImageResource(icon);
    }

    @Override
    public void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(activityReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(locationReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_LOCATION));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(activityReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(locationReceiver);
    }
}
package com.g15.smarthelper;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
    private DetectedActivitiesService activitiesService;
    protected ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            locationService = ((DetectedLocationService.LocalBinder) binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            locationService = null;
        }
    };

    protected ServiceConnection serviceConnection2 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            activitiesService = ((DetectedActivitiesService.LocalBinder) binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            activitiesService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews() {
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
        bindService(new Intent(this, DetectedActivitiesService.class),
                serviceConnection2, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if (locationService != null) {
            unbindService(serviceConnection);
        }
        if (activitiesService != null) {
            unbindService(serviceConnection2);
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
        if (activitiesService != null) {
            activitiesService.startTracking();
        }
    }

    public void removeActivityUpdates() {
        if (activitiesService != null) {
            activitiesService.stopTracking();
        }
    }
}
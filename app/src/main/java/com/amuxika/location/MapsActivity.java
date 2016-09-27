package com.amuxika.location;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private boolean active;
    private BroadcastReceiver broadcastReceiver;
    private static final String LOGTAG = "android-location";

    private static final int REQUEST_TO_LOCALIZATION_DEVICE = 101;
    private static final int REQUEST_CONFIG_LOCATION = 201;
    private Intent intent;
    private ToggleButton updateButton;


    @Override
    public void onResume()
    {
        super.onResume();

        //Return from GPS Network Tracker localization info
        if(broadcastReceiver == null){
            broadcastReceiver = new LocationReceiver();
        }
        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updateButton = (ToggleButton) findViewById(R.id.updateButton);

        active = false;
        updateButton.setChecked(active);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        intent = new Intent(MapsActivity.this, GPSNetworkTracker.class);


        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Check localization permission
                if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    updateButton.setChecked(false);
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_TO_LOCALIZATION_DEVICE);
                } else {

                    active = !active;
                    toggleLocationUpdates(active);
                }

            }
        });

    }


    private void toggleLocationUpdates(boolean enable) {

        if (enable) {

            //enableLocationUpdates();
            startService(intent);

            Toast.makeText(MapsActivity.this, "Buscando localizaciones...", Toast.LENGTH_LONG).show();
        } else {
            stopService(intent);
            //disableLocationUpdates();
            Toast.makeText(MapsActivity.this, "Dejando de buscar localizaciones...", Toast.LENGTH_LONG).show();
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        changeMarker(-34, 151, "Marker in Sydney");
    }

    public void changeMarker(double lat, double lng, String title)
    {
        // Add a marker in Sydney and move the camera
        LatLng select_loc = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(select_loc).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(select_loc));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
        Toast.makeText(getApplicationContext(), "Stop service...", Toast.LENGTH_LONG).show();
        stopService(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_TO_LOCALIZATION_DEVICE) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //Permission granted to get location
                active = !active;
                //updateButton.setT
                toggleLocationUpdates(active);


            } else {
                //Permission denied:


                Log.e(LOGTAG, "Permission denied, check your app configuration please!!");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONFIG_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        toggleLocationUpdates(true);
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(LOGTAG, "User not make correct need configuration changes");
                        break;
                }
                break;
        }
    }
}

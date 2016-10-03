package com.amuxika.location;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
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
    private LocationReceiver broadcastReceiver;
    private static final String LOGTAG = "android-location";

    private static final int REQUEST_TO_LOCALIZATION_DEVICE = 101;
    private static final int REQUEST_CONFIG_LOCATION = 201;
    private Intent intent;
    private ToggleButton updateButton;


    @Override
    public void onResume() {
        super.onResume();


        //Return from GPS Network Tracker localization info (Need to register receiver in manifest)
        if (broadcastReceiver == null) {
            broadcastReceiver = new LocationReceiver();
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
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

                    if (CheckGpsStatus(MapsActivity.this)) {
                        active = !active;
                        updateButton.setChecked(true);
                        toggleLocationUpdates(active);
                    } else {
                        String message_title = "Location configuration";
                        String message = "Active GPS to check your current location. ";
                        String config_btn = "Configurate";
                        showSettingsAlert(message_title, message, config_btn);
                        updateButton.setChecked(false);
                    }

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

    /***********************************************************************************************
     * Default location load when start app
     *
     * @param googleMap: Map object to add markers and info
     **********************************************************************************************/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        changeMarker(-34, 151, "Marker in Sydney");
    }

    public void changeMarker(double lat, double lng, String title) {
        // Add a marker in Sydney and move the camera
        LatLng select_loc = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(select_loc).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(select_loc));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
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

    /**********************************************************
     * @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     * switch (requestCode) {
     * case REQUEST_CONFIG_LOCATION:
     * switch (resultCode) {
     * case Activity.RESULT_OK:
     * toggleLocationUpdates(true);
     * break;
     * case Activity.RESULT_CANCELED:
     * Log.i(LOGTAG, "User not make correct need configuration changes");
     * break;
     * }
     * break;
     * }
     * }
     ***/

    public static boolean CheckGpsStatus(Context context) {

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void showSettingsAlert(String message_title, String message, String config_btn) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle(message_title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // On pressing Settings button
        alertDialog.setPositiveButton(config_btn, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                //mContext.startActivity(intent);
                startActivityForResult(intent, 0);
                overridePendingTransition(0, 0);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Ez dut nahi nire kokapena erabili", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                System.out.println("OUR LOCATION NOT FOUND, USE EIBAR LOCATION");

                dialog.cancel();


                intent = getIntent();
                finish();
                startActivity(intent);
                overridePendingTransition(0, 0);

            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
}

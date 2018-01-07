package com.amuxika.location.activities

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.amuxika.location.R
import com.amuxika.location.receivers.LocationReceiver
import com.amuxika.location.services.LocationService
import com.amuxika.location.services.API25OrMoreLocationService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var active: Boolean = false
    private var broadcastReceiver: LocationReceiver? = null
    private var mMap: GoogleMap? = null
    public override fun onResume() {
        super.onResume()


        //Return from GPS Network Tracker localization info (Need to register receiver in manifest)
        if (broadcastReceiver == null) {
            broadcastReceiver = LocationReceiver()
        }
        registerReceiver(broadcastReceiver, IntentFilter("location_update"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeComponents()


        updateButton.setOnClickListener {
            //Check localization permission
            if (ActivityCompat.checkSelfPermission(this@MapsActivity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                updateButton!!.isChecked = false
                ActivityCompat.requestPermissions(this@MapsActivity,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_TO_LOCALIZATION_DEVICE)
            } else {

                if (CheckGpsStatus(this@MapsActivity)) {
                    active = !active
                    updateButton!!.isChecked = true
                    toggleLocationUpdates(active)
                } else {
                    val message_title = "Location configuration"
                    val message = "Active GPS to check your current location. "
                    val config_btn = "Configurate"
                    showSettingsAlert(message_title, message, config_btn)
                    updateButton!!.isChecked = false
                }
            }
        }

        satelliteFloatingActionButtonItem!!.setOnClickListener {
            selectMapType(1)
            map_options_FloatingActionMenu!!.close(true)
        }
        hybridFloatingActionButtonItem!!.setOnClickListener {
            selectMapType(3)
            map_options_FloatingActionMenu!!.close(true)
        }
        roadmapFloatingActionButtonItem!!.setOnClickListener {
            selectMapType(2)
            map_options_FloatingActionMenu!!.close(true)
        }
        normalFloatingActionButtonItem!!.setOnClickListener {
            selectMapType(4)
            map_options_FloatingActionMenu!!.close(true)
        }

    }

    private fun initializeComponents() {


        active = false
        updateButton!!.isChecked = active

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }


    private fun toggleLocationUpdates(enable: Boolean) {

        map_options_FloatingActionMenu!!.close(true)
        if (enable) {

            //enableLocationUpdates();
            // Check if we're running on Android 5.0 or higher
            if (Build.VERSION.SDK_INT >= 25) {
                // Call some material design APIs here
                startService(Intent(this@MapsActivity, API25OrMoreLocationService::class.java))
            } else {
                startService(Intent(this@MapsActivity, LocationService::class.java))
            }

            updateButton.isChecked = true
            Toast.makeText(this@MapsActivity, "Buscando localizaciones...", Toast.LENGTH_LONG).show()
        } else {
            if (Build.VERSION.SDK_INT >= 25) {
                // Call some material design APIs here
                println("LOCATION SERVICE")
                stopService(Intent(this@MapsActivity, API25OrMoreLocationService::class.java))
            } else {
                println("GPSNETWORK TRACKER SERVICE")
                stopService(Intent(this@MapsActivity, LocationService::class.java))
            }
            //disableLocationUpdates();
            Toast.makeText(this@MapsActivity, "Dejando de buscar localizaciones...", Toast.LENGTH_LONG).show()
            updateButton!!.isChecked = false
        }

    }

    /***********************************************************************************************
     * Default location load when start app

     * @param googleMap: Map object to add markers and info
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        changeMarker(-34.0, 151.0, "Marker in Sydney")
        mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL

        mMap!!.uiSettings.isZoomControlsEnabled = true
    }

    private fun selectMapType(type: Int) {
        when (type) {
            1 -> mMap!!.mapType = GoogleMap.MAP_TYPE_SATELLITE
            2 -> mMap!!.mapType = GoogleMap.MAP_TYPE_TERRAIN
            3 -> mMap!!.mapType = GoogleMap.MAP_TYPE_HYBRID
            else -> mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        }
    }


    fun changeMarker(lat: Double, lng: Double, title: String) {
        // Add a marker in Sydney and move the camera
        val select_loc = LatLng(lat, lng)
        mMap!!.addMarker(MarkerOptions().position(select_loc).title(title))
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(select_loc))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver)
        }
        Toast.makeText(applicationContext, "Stop service...", Toast.LENGTH_LONG).show()
        if (Build.VERSION.SDK_INT >= 25) {
            // Call some material design APIs here
            println("LOCATION SERVICE")
            stopService(Intent(this@MapsActivity, API25OrMoreLocationService::class.java))
        } else {
            println("GPSNETWORK TRACKER SERVICE")
            stopService(Intent(this@MapsActivity, LocationService::class.java))
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_TO_LOCALIZATION_DEVICE) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //Permission granted to get location
                active = !active
                //updateButton.setT
                toggleLocationUpdates(active)


            } else {
                //Permission denied:


                Log.e(LOGTAG, "Permission denied, check your app configuration please!!")
            }
        }
    }

    fun showSettingsAlert(message_title: String, message: String, config_btn: String) {
        val alertDialog = AlertDialog.Builder(this)

        // Setting Dialog Title
        alertDialog.setTitle(message_title)

        // Setting Dialog Message
        alertDialog.setMessage(message)

        // On pressing Settings button
        alertDialog.setPositiveButton(config_btn) { dialog, which ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            //mContext.startActivity(intent);
            startActivityForResult(intent, 0)
            overridePendingTransition(0, 0)
        }

        // on pressing cancel button
        alertDialog.setNegativeButton("Ez dut nahi nire kokapena erabili") { dialog, which ->
            // println("OUR LOCATION NOT FOUND, USE EIBAR LOCATION")

            dialog.cancel()


            val intent = getIntent()
            finish()
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        // Showing Alert Message
        alertDialog.show()
    }

    companion object {
        private val LOGTAG = "android-location"

        private val REQUEST_TO_LOCALIZATION_DEVICE = 101
        private val REQUEST_CONFIG_LOCATION = 201

        /**********************************************************
         * @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         * * switch (requestCode) {
         * * case REQUEST_CONFIG_LOCATION:
         * * switch (resultCode) {
         * * case Activity.RESULT_OK:
         * * toggleLocationUpdates(true);
         * * break;
         * * case Activity.RESULT_CANCELED:
         * * Log.i(LOGTAG, "User not make correct need configuration changes");
         * * break;
         * * }
         * * break;
         * * }
         * * }
         */

        fun CheckGpsStatus(context: Context): Boolean {

            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
    }
}

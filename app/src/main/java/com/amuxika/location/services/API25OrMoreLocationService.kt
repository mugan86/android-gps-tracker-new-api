package com.amuxika.location.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*

/***************************************************************************************************
 * Created by Fernando Gabriel on 04/01/18.
 *
 * GPS Tracker service to stablished our current location. Use in versions >= 25
 **************************************************************************************************/
open class API25OrMoreLocationService : Service() {

    companion object {
        private val UPDATE_INTERVAL = (10 * 1000).toLong()  /* 10 secs */
        private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */
        private val LOGSERVICE = "#######"
        val LOCATION_UPDATE = "location_update"
    }

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mLocationCallback: LocationCallback? = null
    private lateinit var mLocationRequest:LocationRequest

    override fun onCreate() {
        super.onCreate()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationCallback = object:LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // do work here
                onLocationChanged(locationResult.lastLocation)
            }
        }

        getLastLocation()
        initLocationRequest()
        startLocationUpdate()
        Log.i(LOGSERVICE, "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(LOGSERVICE, "onDestroy - Stop location updates ")
        stopLocationUpdate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(LOGSERVICE, "onStartCommand")

        return Service.START_STICKY
    }

    override fun onBind(intent:Intent): IBinder? = null

    @SuppressLint("MissingPermission")
    private fun startLocationUpdate() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()
        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
    }

    fun onLocationChanged(location: Location) {
        //Send change location info to LocationReceiver
        val i = Intent(LOCATION_UPDATE)
        i.putExtra(LOCATION_UPDATE, location)
        sendBroadcast(i)
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        mFusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    // GPS location can be null if GPS is switched off
                    if (location != null) {
                        onLocationChanged(location)
                    }
                }
                .addOnFailureListener { e ->
                    Log.d("MapDemoActivity", "Error trying to get last GPS location")
                    e.printStackTrace()
                }
    }

    private fun stopLocationUpdate() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
    }
    @Synchronized private fun initLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = UPDATE_INTERVAL
        mLocationRequest.fastestInterval = FASTEST_INTERVAL
        mLocationRequest.smallestDisplacement = 1000f //Comprobar al km
    }
}
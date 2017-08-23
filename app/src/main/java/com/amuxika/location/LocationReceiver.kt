package com.amuxika.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast


/*********************************************************
 * Created by anartzmugika on 27/9/16. Update 5/12/2016
 */
class LocationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        println("Data receive!...")

        sendDataToCurrentActivity(java.lang.Double.parseDouble(intent.extras.get("lat")!!.toString()),
                java.lang.Double.parseDouble(intent.extras.get("lng")!!.toString()), context)

        println("Current location " + intent.extras.get("coordinates")!!.toString())

    }

    fun sendDataToCurrentActivity(lat: Double, lng: Double, context: Context) {
        if (context.javaClass == MapsActivity::class.java) (context as MapsActivity).changeMarker(lat, lng, "My location")
    }
}

package com.amuxika.location.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import com.amuxika.location.activities.MapsActivity
import com.amuxika.location.services.API25OrMoreLocationService


/*********************************************************
 * Created by anartzmugika on 27/9/16. Update 5/12/2016
 */
class LocationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        println("Data receive!...")

        val location = intent.getParcelableExtra<Location>(API25OrMoreLocationService.LOCATION_UPDATE)

        if (location != null) {

            sendDataToCurrentActivity(location, context)
        }

        /*sendDataToCurrentActivity(java.lang.Double.parseDouble(intent.extras.get("lat")!!.toString()),
                java.lang.Double.parseDouble(intent.extras.get("lng")!!.toString()), context)*/

        println("Current location ${location.latitude} / ${location.longitude}")

    }

    private fun sendDataToCurrentActivity(location: Location, context: Context) {
        if (context.javaClass == MapsActivity::class.java)
            (context as MapsActivity).changeMarker(location.latitude, location.longitude, "My location")
    }
}

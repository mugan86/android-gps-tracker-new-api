package com.amuxika.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


/*********************************************************
 * Created by anartzmugika on 27/9/16.
 */
public class LocationReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {

        System.out.println("Datos recibidos...");


        ((MapsActivity)context).changeMarker(Double.parseDouble(intent.getExtras().get("lat").toString()),
                Double.parseDouble(intent.getExtras().get("lng").toString()), "My location");
        Toast.makeText(context, intent.getExtras().get("coordinates").toString(), Toast.LENGTH_LONG).show();
        System.out.println("Current location " + intent.getExtras().get("coordinates").toString());


    }
}

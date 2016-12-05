package com.amuxika.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


/*********************************************************
 * Created by anartzmugika on 27/9/16. Update 5/12/2016
 ****************/
public class LocationReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {

        System.out.println("Data receive!...");

        sendDataToCurrentActivity(Double.parseDouble(intent.getExtras().get("lat").toString()),
                Double.parseDouble(intent.getExtras().get("lng").toString()), context);

        System.out.println("Current location " + intent.getExtras().get("coordinates").toString());

    }

    public void sendDataToCurrentActivity(double lat, double lng, Context context)
    {
        if (context.getClass() == MapsActivity.class) ((MapsActivity)context).changeMarker(lat, lng, "My location");
    }
}

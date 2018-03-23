package com.cybercom.passenger.helpers;

import android.location.Location;

public class LocationHelper {
    public static String  convertLocationToDisplayString(Location location){
        double latD = location.getLatitude();
        double lngD = location.getLongitude();
        String lat = String.valueOf(latD);
        String lng = String.valueOf(lngD);
        return lat + "," + lng;
    }
}

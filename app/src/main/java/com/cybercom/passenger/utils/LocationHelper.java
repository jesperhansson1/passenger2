package com.cybercom.passenger.utils;

import android.location.Location;

import com.cybercom.passenger.model.Position;

public class LocationHelper {
    public static String convertLocationToDisplayString(Location location){
        if (location != null) {
            double latD = location.getLatitude();
            double lngD = location.getLongitude();
            String lat = String.valueOf(latD);
            String lng = String.valueOf(lngD);
            return lat + "," + lng;
        } else {
            return "0,0";
        }
    }

    public static Position getPositionFromString(String locationValue)
    {
        double lat,lang;
        String[] locArr = locationValue.split(",");
        lat = Double.valueOf(locArr[0]);
        lang = Double.valueOf(locArr[1]);
        return new Position(lat,lang);
    }
}

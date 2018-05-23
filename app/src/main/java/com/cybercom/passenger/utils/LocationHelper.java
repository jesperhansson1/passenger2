package com.cybercom.passenger.utils;

import android.location.Location;

import com.cybercom.passenger.model.Position;
import com.google.android.gms.maps.model.LatLng;
import com.javadocmd.simplelatlng.Geohasher;

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

    /*public static Position getPositionFromString(String locationValue)
    {
        double lat,lang;
        String[] locArr = locationValue.split(",");
        lat = Double.valueOf(locArr[0]);
        lang = Double.valueOf(locArr[1]);
        return new Position(lat,lang);
    }*/

    public static String getStringFromPosition(Position position) {
        String lat = String.valueOf(position.getLatitude());
        String lng = String.valueOf(position.getLatitude());

        return lat + ',' + lng;
    }

    public static String getStringFromLatLng(double lat, double lng) {
        return String.valueOf(lat) + ',' + String.valueOf(lng);
    }

    public static Position convertLocationToPosition(Location location){
       return new Position(hashLocation(location),location.getLatitude(),location.getLongitude());
    }

    public static Location convertLatLngToLocation(LatLng latLng){
        Location location = new Location("fromLatLng");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        return location;
    }

    public static Location convertPositionToLocation(Position position){
        Location location = new Location("fromPosition");
        location.setLatitude(position.getLatitude());
        location.setLongitude(position.getLongitude());
        return location;
    }


    private static String hashLocation(Location location){
        return Geohasher.hash(new com.javadocmd.simplelatlng.LatLng(location.getLatitude(),location.getLongitude()));
    }


}

package com.cybercom.passenger.utils;

public class MockLocation {

    double lat;
    double lng;

    public MockLocation(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    public String toString() {
        return "(" + lat + "," + lng + ")";
    }
}

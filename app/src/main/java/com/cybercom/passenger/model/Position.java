package com.cybercom.passenger.model;

import java.io.Serializable;

public class Position implements Serializable {

    private String mGeoHash;
    private double mLatitude;
    private double mLongitude;

    public Position(){

    }

    public Position(String geoHash, double latitude, double longitude){
        mGeoHash = geoHash;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public String getGeoHash() {
        return mGeoHash;
    }

    public void setGeoHash(String geoHash) {
        mGeoHash = geoHash;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    @Override
    public String toString() {
        return "Position{" +
                "mGeoHash='" + mGeoHash + '\'' +
                ", mLatitude=" + mLatitude +
                ", mLongitude=" + mLongitude +
                '}';
    }
}


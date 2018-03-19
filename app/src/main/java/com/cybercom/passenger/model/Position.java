package com.cybercom.passenger.model;

public class Position {

    private double mLatitude;
    private double mLongitude;

    public Position(){

    }

    public Position(double latitude, double longitude){
        mLatitude = latitude;
        mLongitude = longitude;
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
                "mLatitude='" + mLatitude + '\'' +
                ", mLongitude='" + mLongitude + '\'' +
                '}';
    }
}


package com.cybercom.passenger.model;

public class Position {

    private String mLatitude;
    private String mLongitude;

    public Position(){

    }

    public Position(String latitude, String longitude){
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public String getLatitude() {
        return mLatitude;
    }

    public void setLatitude(String latitude) {
        mLatitude = latitude;
    }

    public String getLongitude() {
        return mLongitude;
    }

    public void setLongitude(String longitude) {
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


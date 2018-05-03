package com.cybercom.passenger.model;

public class RideFare {
    String mStartLocation;
    String mEndLocation;
    String mDistance;
    String mDuration;

    public RideFare(String startLocation, String endLocation, String distance, String duration) {
        mStartLocation = startLocation;
        mEndLocation = endLocation;
        mDistance = distance;
        mDuration = duration;
    }

    public String getStartLocation() {
        return mStartLocation;
    }

    public void setStartLocation(String startLocation) {
        mStartLocation = startLocation;
    }

    public String getEndLocation() {
        return mEndLocation;
    }

    public void setEndLocation(String endLocation) {
        mEndLocation = endLocation;
    }

    public String getDistance() {
        return mDistance;
    }

    public void setDistance(String distance) {
        mDistance = distance;
    }

    public String getDuration() {
        return mDuration;
    }

    public void setDuration(String duration) {
        mDuration = duration;
    }
}

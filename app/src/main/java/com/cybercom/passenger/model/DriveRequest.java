package com.cybercom.passenger.model;

import android.location.Location;

public class DriveRequest {

    private long mTime;
    private Location mStartLocation;
    private Location mEndLocation;
    private String mNotificationTokenId;
    private int mExtraPassengers;

    public DriveRequest(long time, Location startLocation, Location endLocation, String notificationTokenId, int extraPassengers) {
        this.mTime = time;
        this.mStartLocation = startLocation;
        this.mEndLocation = endLocation;
        this.mNotificationTokenId = notificationTokenId;
        this.mExtraPassengers = extraPassengers;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        this.mTime = time;
    }

    public Location getStartLocation() {
        return mStartLocation;
    }

    public void setStartLocation(Location startLocation) {
        this.mStartLocation = startLocation;
    }

    public Location getEndLocation() {
        return mEndLocation;
    }

    public void setEndLocation(Location endLocation) {
        this.mEndLocation = endLocation;
    }

    public String getNotificationTokenId() {
        return mNotificationTokenId;
    }

    public void setNotificationTokenId(String notificationTokenId) {
        this.mNotificationTokenId = notificationTokenId;
    }

    public int getExtraPassengers() {
        return mExtraPassengers;
    }

    public void setExtraPassengers(int extraPassengers) {
        this.mExtraPassengers = extraPassengers;
    }
}

package com.cybercom.passenger.model;

import android.location.Location;

public class Drive {

    private long mTime;
    private Location mStartLocation;
    private Location mEndLocation;
    private String mNotificationTokenId;
    private int mAvailableSeats;

    public Drive() {
    }

    public Drive(long time, Location startLocation, Location endLocation, String notificationTokenId, int availableSeats) {
        mTime = time;
        mStartLocation = startLocation;
        mEndLocation = endLocation;
        mNotificationTokenId = notificationTokenId;
        mAvailableSeats = availableSeats;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public Location getStartLocation() {
        return mStartLocation;
    }

    public void setStartLocation(Location startLocation) {
        mStartLocation = startLocation;
    }

    public Location getEndLocation() {
        return mEndLocation;
    }

    public void setEndLocation(Location endLocation) {
        mEndLocation = endLocation;
    }

    public String getNotificationTokenId() {
        return mNotificationTokenId;
    }

    public void setNotificationTokenId(String notificationTokenId) {
        mNotificationTokenId = notificationTokenId;
    }

    public int getAvailableSeats() {
        return mAvailableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        mAvailableSeats = availableSeats;
    }

    @Override
    public String toString() {
        return "Drive{" +
                "mTime=" + mTime +
                ", mStartLocation=" + mStartLocation +
                ", mEndLocation=" + mEndLocation +
                ", mNotificationTokenId='" + mNotificationTokenId + '\'' +
                ", mAvailableSeats=" + mAvailableSeats +
                '}';
    }
}

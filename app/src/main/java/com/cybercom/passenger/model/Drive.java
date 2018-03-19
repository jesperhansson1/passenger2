package com.cybercom.passenger.model;

import android.location.Location;

public class Drive {

    private Long mTime;
    private Location mStartLocation;
    private Location mEndLocation;
    private String mNotificationTokenId;
    private int mAvailableSeats;

    public Drive(Long time, Location startLocation, Location endLocation, String notificationTokenId, int availableSeats) {
        this.mTime = time;
        this.mStartLocation = startLocation;
        this.mEndLocation = endLocation;
        this.mNotificationTokenId = notificationTokenId;
        this.mAvailableSeats = availableSeats;
    }

    public Long getTime() {
        return mTime;
    }

    public void setTime(Long time) {
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

    public int getAvailableSeats() {
        return mAvailableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.mAvailableSeats = availableSeats;
    }
}

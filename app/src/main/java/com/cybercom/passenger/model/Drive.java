package com.cybercom.passenger.model;

public class Drive {

    private String mTime;
    private String mStartLocation;
    private String mEndLocation;
    private String mNotificationTokenId;
    private int mAvailableSeats;

    public Drive(String time, String startLocation, String endLocation, String notificationTokenId, int availableSeats) {
        this.mTime = time;
        this.mStartLocation = startLocation;
        this.mEndLocation = endLocation;
        this.mNotificationTokenId = notificationTokenId;
        this.mAvailableSeats = availableSeats;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        this.mTime = time;
    }

    public String getStartLocation() {
        return mStartLocation;
    }

    public void setStartLocation(String startLocation) {
        this.mStartLocation = startLocation;
    }

    public String getEndLocation() {
        return mEndLocation;
    }

    public void setEndLocation(String endLocation) {
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

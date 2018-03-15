package com.cybercom.passenger.model;

public class DriveRequest {

    private String mTime;
    private String mStartLocation;
    private String mEndLocation;
    private String mNotificationTokenId;
    private String mExtraPassengers;

    public DriveRequest(String time, String startLocation, String endLocation, String notificationTokenId, String extraPassengers) {
        this.mTime = time;
        this.mStartLocation = startLocation;
        this.mEndLocation = endLocation;
        this.mNotificationTokenId = notificationTokenId;
        this.mExtraPassengers = extraPassengers;
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

    public String getExtraPassengers() {
        return mExtraPassengers;
    }

    public void setExtraPassengers(String extraPassengers) {
        this.mExtraPassengers = extraPassengers;
    }
}

package com.cybercom.passenger.model;

public class DriveRequest {

    private long mTime;
    private Position mStartLocation;
    private Position mEndLocation;
    private String mNotificationTokenId;
    private int mExtraPassengers;

    public DriveRequest(){

    }

    public DriveRequest(long time, Position startLocation, Position endLocation, String notificationTokenId, int extraPassengers) {
        mTime = time;
        mStartLocation = startLocation;
        mEndLocation = endLocation;
        mNotificationTokenId = notificationTokenId;
        mExtraPassengers = extraPassengers;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public Position getStartLocation() {
        return mStartLocation;
    }

    public void setStartLocation(Position startLocation) {
        mStartLocation = startLocation;
    }

    public Position getEndLocation() {
        return mEndLocation;
    }

    public void setEndLocation(Position endLocation) {
        mEndLocation = endLocation;
    }

    public String getNotificationTokenId() {
        return mNotificationTokenId;
    }

    public void setNotificationTokenId(String notificationTokenId) {
        mNotificationTokenId = notificationTokenId;
    }

    public int getExtraPassengers() {
        return mExtraPassengers;
    }

    public void setExtraPassengers(int extraPassengers) {
        mExtraPassengers = extraPassengers;
    }

    @Override
    public String toString() {
        return "DriveRequest{" +
                "mTime=" + mTime +
                ", mStartLocation=" + mStartLocation +
                ", mEndLocation=" + mEndLocation +
                ", mNotificationTokenId='" + mNotificationTokenId + '\'' +
                ", mExtraPassengers=" + mExtraPassengers +
                '}';
    }
}

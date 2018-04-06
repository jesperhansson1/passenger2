package com.cybercom.passenger.model;

import java.io.Serializable;

public class DriveRequest implements Serializable {

    private User mPassenger;

    private long mTime;
    private Position mStartLocation;
    private Position mEndLocation;
    private int mExtraPassengers;

    public DriveRequest(){
    }

    public DriveRequest(User passenger, long time, Position startLocation, Position endLocation, int extraPassengers) {
        mPassenger = passenger;
        mTime = time;
        mStartLocation = startLocation;
        mEndLocation = endLocation;
        mExtraPassengers = extraPassengers;
    }

    public User getPassenger() {
        return mPassenger;
    }

    public void setPassenger(User passenger) {
        mPassenger = passenger;
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
                ", mPassenger=" + mPassenger +
                ", mStartLocation=" + mStartLocation +
                ", mEndLocation=" + mEndLocation +
                ", mExtraPassengers=" + mExtraPassengers +
                '}';
    }
}

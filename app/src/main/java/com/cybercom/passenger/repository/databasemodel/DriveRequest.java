package com.cybercom.passenger.repository.databasemodel;

import com.cybercom.passenger.model.Position;

import java.io.Serializable;

public class DriveRequest implements Serializable {

    private String mPassengerId;

    private long mTime;
    private Position mStartLocation;
    private Position mEndLocation;
    private int mExtraPassengers;

    public DriveRequest(){
    }

    public DriveRequest(String passengerId, long time, Position startLocation, Position endLocation, int extraPassengers) {
        mPassengerId = passengerId;
        mTime = time;
        mStartLocation = startLocation;
        mEndLocation = endLocation;
        mExtraPassengers = extraPassengers;
    }

    public String getPassenger() {
        return mPassengerId;
    }

    public void setPassenger(String passengerId) {
        mPassengerId = passengerId;
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
                ", mPassengerId=" + mPassengerId +
                ", mStartLocation=" + mStartLocation +
                ", mEndLocation=" + mEndLocation +
                ", mExtraPassengers=" + mExtraPassengers +
                '}';
    }
}

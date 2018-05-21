package com.cybercom.passenger.model;

import java.io.Serializable;

public class Drive implements Serializable {

    private User mDriver;

    private String mId;
    private long mTime;
    private Position mStartLocation;
    private Position mEndLocation;
    private Position mCurrentPosition;
    private float mCurrentVelocity;
    private int mAvailableSeats;


    public Drive(){}

    public Drive(String id, User driver, long time, Position startLocation, Position endLocation,
                 int availableSeats, Position currentPosition, float currentVelocity) {
        mId = id;
        mDriver = driver;
        mTime = time;
        mStartLocation = startLocation;
        mEndLocation = endLocation;
        mAvailableSeats = availableSeats;
        mCurrentPosition = currentPosition;
        mCurrentVelocity = currentVelocity;
    }

    public User getDriver() {
        return mDriver;
    }

    public void setDriver(User driver) {
        this.mDriver = driver;
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
                ", mDriver=" + mDriver +
                ", mStartLocation=" + mStartLocation +
                ", mEndLocation=" + mEndLocation +
                ", mAvailableSeats=" + mAvailableSeats +
                ", mCurrentPosition=" + mCurrentPosition +
                ", mCurrentVelocity=" + mCurrentVelocity +
                '}';
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public Position getCurrentPosition() {
        return mCurrentPosition;
    }

    public void setCurrentPosition(Position currentPosition) {
        this.mCurrentPosition = currentPosition;
    }

    public float getCurrentVelocity() {
        return mCurrentVelocity;
    }

    public void setCurrentVelocity(float currentVelocity) {
        this.mCurrentVelocity = currentVelocity;
    }
}

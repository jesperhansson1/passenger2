package com.cybercom.passenger.repository.databasemodel;

import com.cybercom.passenger.model.Position;

public class Drive {

    private String mDriverId;
    private long mTime;
    private Position mStartLocation;
    private Position mEndLocation;
    private int mAvailableSeats;

    public Drive() {}

    public Drive(String driverId, long time, Position startLocation, Position endLocation, int availableSeats) {
        mDriverId = driverId;
        mTime = time;
        mStartLocation = startLocation;
        mEndLocation = endLocation;
        mAvailableSeats = availableSeats;
    }

    public String getDriverId() {
        return mDriverId;
    }

    public void setDriverId(String driverId) {
        mDriverId = driverId;
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
                "mDriverId=" + mDriverId +
                ", mTime=" + mTime +
                ", mStartLocation=" + mStartLocation +
                ", mEndLocation=" + mEndLocation +
                ", mAvailableSeats=" + mAvailableSeats +
                '}';
    }
}

package com.cybercom.passenger.model;

public class PassengerRide {

    private String mId;
    private String mDriveId;
    private String mPassegnerId;
    private Position mPassengerPos;

    public PassengerRide() {
    }

    public PassengerRide(String id, String driveId, String passengerId, Position passengerPos) {
        mId = id;
        mDriveId = driveId;
        mPassegnerId = passengerId;
        mPassengerPos = passengerPos;
    }

    public String getDriveId() {
        return mDriveId;
    }

    public void setDriveId(String driveId) {
        mDriveId = driveId;
    }

    public String getPassegnerId() {
        return mPassegnerId;
    }

    public void setPassegnerId(String passegnerId) {
        mPassegnerId = passegnerId;
    }

    public Position getPassengerPos() {
        return mPassengerPos;
    }

    public void setPassengerPos(Position passengerPos) {
        mPassengerPos = passengerPos;
    }

    @Override
    public String toString() {
        return "PassengerRide{" +
                "mDriveId='" + mDriveId + '\'' +
                ", mPassegnerId='" + mPassegnerId + '\'' +
                ", mPassengerPos=" + mPassengerPos +
                '}';
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }
}

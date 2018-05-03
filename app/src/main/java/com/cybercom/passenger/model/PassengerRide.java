package com.cybercom.passenger.model;

public class PassengerRide {

    private String mDriveId;
    private String mPassegnerId;
    private Position mPassengerPos;

    public PassengerRide() {
    }

    public PassengerRide(String driveId, String passegnerId, Position passengerPos) {
        mDriveId = driveId;
        mPassegnerId = passegnerId;
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

}

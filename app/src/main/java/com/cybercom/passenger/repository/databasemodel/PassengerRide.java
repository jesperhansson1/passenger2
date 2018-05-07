package com.cybercom.passenger.repository.databasemodel;

import com.cybercom.passenger.model.Position;

public class PassengerRide {

    private String mDriveId;
    private String mPassengerId;
    private Position mPassengerPos;

    public PassengerRide() {
    }

    public PassengerRide(String driveId, String passengerId, Position passengerPos) {
        mDriveId = driveId;
        mPassengerId = passengerId;
        mPassengerPos = passengerPos;
    }

    public String getDriveId() {
        return mDriveId;
    }

    public void setDriveId(String driveId) {
        mDriveId = driveId;
    }

    public String getPassengerId() {
        return mPassengerId;
    }

    public void setPassengerId(String passegnerId) {
        mPassengerId = passegnerId;
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
                ", mPassegnerId='" + mPassengerId + '\'' +
                ", mPassengerPos=" + mPassengerPos +
                '}';
    }

}
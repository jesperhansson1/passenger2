package com.cybercom.passenger.repository.databasemodel;

import com.cybercom.passenger.model.Position;

public class PassengerRide {

    private String mDriveId;
    private String mPassengerId;
    private Position mPosition;

    public PassengerRide() {
    }

    public PassengerRide(String driveId, String passengerId, Position position) {
        mDriveId = driveId;
        mPassengerId = passengerId;
        mPosition = position;
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

    public Position getPosition() {
        return mPosition;
    }

    public void setPosition(Position passengerPos) {
        mPosition = passengerPos;
    }

    @Override
    public String toString() {
        return "PassengerRide{" +
                "mDriveId='" + mDriveId + '\'' +
                ", mPassegnerId='" + mPassengerId + '\'' +
                ", mPosition=" + mPosition +
                '}';
    }

}

package com.cybercom.passenger.repository.databasemodel;

import com.cybercom.passenger.model.Position;

import java.io.Serializable;

public class PassengerRide implements Serializable {

    private String mDriveId;
    private String mPassengerId;
    private String  mPassengerPositionId;
    private Position mPickUpPosition;
    private Position mDropOffPosition;
    private boolean mPickUpConfirmed;
    private boolean mDropOffConfirmed;

    public PassengerRide() {
    }
    
    public PassengerRide(String driveId, String passengerId, String passengerPositionId, Position pickUpPosition, Position dropOffPosition) {
        mDriveId = driveId;
        mPassengerId = passengerId;
        mPassengerPositionId = passengerPositionId;
        mPickUpPosition = pickUpPosition;
        mDropOffPosition = dropOffPosition;
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

    public String getPassengerPositionId() {
        return mPassengerPositionId;
    }

    public void setPassengerPositionId(String passengerPositionId) {
        mPassengerPositionId = passengerPositionId;
    }

    @Override
    public String toString() {
        return "PassengerRide{" +
                "mDriveId='" + mDriveId + '\'' +
                ", mPassegnerId='" + mPassengerId + '\'' +
                ", mPassengerPositionId=" + mPassengerPositionId + '\'' +
                ", mPickUpPosition=" + mPickUpPosition + '\'' +
                ", mDropOffPosition=" + mDropOffPosition + '\'' +
                ", mPickUpConfirmed=" + mPickUpConfirmed + '\'' +
                ", mDropOffConfirmed=" + mDropOffConfirmed + '\'' +
                '}';
    }

    public Position getPickUpPosition() {
        return mPickUpPosition;
    }

    public void setPickUpPosition(Position pickUpPosition) {
        this.mPickUpPosition = pickUpPosition;
    }

    public Position getDropOffPosition() {
        return mDropOffPosition;
    }

    public void setDropOffPosition(Position dropOffPosition) {
        this.mDropOffPosition = dropOffPosition;
    }

    public boolean isPickUpConfirmed() {
        return mPickUpConfirmed;
    }

    public void setPickUpConfirmed(boolean pickUpConfirmed) {
        this.mPickUpConfirmed = pickUpConfirmed;
    }

    public boolean isDropOffConfirmed() {
        return mDropOffConfirmed;
    }

    public void setDropOffConfirmed(boolean dropOffConfirmed) {
        this.mDropOffConfirmed = dropOffConfirmed;
    }
}

package com.cybercom.passenger.model;

public class PassengerRide {

    private String mId;
    private String mDriveId;
    private String mPassegnerId;
    private String mPassengerPosId;
    private Position mPickUpPosition;
    private Position mDropOffPosition;
    private boolean mPickUpConfirmed;
    private boolean mDropOffConfirmed;

    public PassengerRide() {
    }

    public PassengerRide(String id, String driveId, String passengerId, String passengerPosId) {
        mId = id;
        mDriveId = driveId;
        mPassegnerId = passengerId;
        mPassengerPosId = passengerPosId;
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

    public String getPassengerPosId() {
        return mPassengerPosId;
    }

    public void setPassengerPosId(String passengerPosId) {
        mPassengerPosId = passengerPosId;
    }

    @Override
    public String toString() {
        return "PassengerRide{" +
                "mId='" + mId + '\'' +
                ", mDriveId='" + mDriveId + '\'' +
                ", mPassegnerId='" + mPassegnerId + '\'' +
                ", mPassengerPosId=" + mPassengerPosId + '\'' +
                ", mPickUpPosition=" + mPickUpPosition + '\'' +
                ", mDropOffPosition=" + mDropOffPosition + '\'' +
                ", mPickUpConfirmed=" + mPassengerPosId + '\'' +
                ", mDropOffConfirmed=" + mDropOffConfirmed +
                '}';
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public Position getPickUpPosition() {
        return mPickUpPosition;
    }

    public void setPickUpPosition(Position mPickUpPosition) {
        this.mPickUpPosition = mPickUpPosition;
    }

    public Position getDropOffPosition() {
        return mDropOffPosition;
    }

    public void setDropOffPosition(Position mDropOffPosition) {
        this.mDropOffPosition = mDropOffPosition;
    }

    public boolean isPickUpConfirmed() {
        return mPickUpConfirmed;
    }

    public void setPickUpConfirmed(boolean mPickUpConfirmed) {
        this.mPickUpConfirmed = mPickUpConfirmed;
    }

    public boolean isDropOffConfirmed() {
        return mDropOffConfirmed;
    }

    public void setDropOffConfirmed(boolean mDropOffConfirmed) {
        this.mDropOffConfirmed = mDropOffConfirmed;
    }
}

package com.cybercom.passenger.model;

import java.io.Serializable;

public class PassengerRide implements Serializable {

    private String mId;
    private Drive mDrive;
    private User mPassenger;
    private Position mPickUpPosition;
    private Position mDropOffPosition;
    private boolean mPickUpConfirmed;
    private boolean mDropOffConfirmed;
    private String mStartAddress;
    private String mEndAddress;
    private boolean mCancelled;
    private String mChargeId;

    public PassengerRide() {
    }

    public PassengerRide(String id, Drive drive, User passenger, Position pickUpPosition,
                         Position dropOffPosition, boolean pickUpConfirmed,
                         boolean dropOffConfirmed, String startAddress, String endAddress,
                         boolean cancelled, String chargeId) {
        mId = id;
        mDrive = drive;
        mPassenger = passenger;
        mPickUpPosition = pickUpPosition;
        mDropOffPosition = dropOffPosition;
        mPickUpConfirmed = pickUpConfirmed;
        mDropOffPosition = dropOffPosition;
        mDropOffConfirmed = dropOffConfirmed;
        mStartAddress = startAddress;
        mEndAddress = endAddress;
        mCancelled = cancelled;
        mChargeId = chargeId;
    }

    public Drive getDrive() {
        return mDrive;
    }

    public void setDrive(Drive drive) {
        mDrive = drive;
    }

    public User getPassenger() {
        return mPassenger;
    }

    public void setPassengerId(User passenger) {
        mPassenger = passenger;
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

    public String getStartAddress() {
        return mStartAddress;
    }

    public void setStartAddress(String startAddress) {
        mStartAddress = startAddress;
    }

    public String getEndAddress() {
        return mEndAddress;
    }

    public void setEndAddress(String endAddress) {
        mEndAddress = endAddress;
    }

    public boolean isCancelled() {
        return mCancelled;
    }

    public void setCancelled(boolean mCancelled) {
        this.mCancelled = mCancelled;
    }

    @Override
    public String toString() {
        return "PassengerRide{" +
                "mId='" + mId + '\'' +
                ", mDrive='" + mDrive + '\'' +
                ", mPassenger='" + mPassenger + '\'' +
                ", mPickUpPosition=" + mPickUpPosition + '\'' +
                ", mDropOffPosition=" + mDropOffPosition + '\'' +
                ", mDropOffConfirmed=" + mDropOffConfirmed + '\'' +
                ", mStartAddress=" + mStartAddress + '\'' +
                ", mEndAddress=" + mEndAddress + '\'' +
                ", mChargeId=" + mChargeId +
                '}';
    }

    public String getChargeId() {   return mChargeId;   }

    public void setChargeId(String chargeId) {  mChargeId = chargeId;   }
}

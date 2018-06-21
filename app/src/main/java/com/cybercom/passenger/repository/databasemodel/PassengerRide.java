package com.cybercom.passenger.repository.databasemodel;

import com.cybercom.passenger.model.Position;

import java.io.Serializable;

public class PassengerRide implements Serializable {

    private String mDriveId;
    private String mPassengerId;
    private Position mPickUpPosition;
    private Position mDropOffPosition;
    private boolean mPickUpConfirmed;
    private boolean mDropOffConfirmed;
    private boolean mCancelled;
    private String mStartAddress;
    private String mEndAddress;
    private String mChargeId;
    private double mPrice;

    public PassengerRide() {
    }

    public PassengerRide(String driveId, String passengerId, Position pickUpPosition,
                         Position dropOffPosition,  boolean pickUpConfirmed,
                         boolean dropOffConfirmed, String startAddress, String endAddress,
                         String chargeId, double price) {
        mDriveId = driveId;
        mPassengerId = passengerId;
        mPickUpPosition = pickUpPosition;
        mDropOffPosition = dropOffPosition;
        mPickUpConfirmed = pickUpConfirmed;
        mDropOffConfirmed = dropOffConfirmed;
        mStartAddress = startAddress;
        mEndAddress = endAddress;
        mChargeId = chargeId;
        mPrice = price;
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

    public void setStartAddress(String startAddress) {
        mStartAddress = startAddress;
    }

    public void setEndAddress(String endAddress) {
        mEndAddress = endAddress;
    }

    public String getStartAddress() {
        return mStartAddress;
    }

    public String getEndAddress() {
        return mEndAddress;
    }


    @Override
    public String toString() {
        return "PassengerRide{" +
                "mDriveId='" + mDriveId + '\'' +
                ", mPassegnerId='" + mPassengerId + '\'' +
                ", mPickUpPosition=" + mPickUpPosition + '\'' +
                ", mDropOffPosition=" + mDropOffPosition + '\'' +
                ", mPickUpConfirmed=" + mPickUpConfirmed + '\'' +
                ", mDropOffConfirmed=" + mDropOffConfirmed + '\'' +
                ", mDropOffConfirmed=" + mCancelled + '\'' +
                ", mChargeId=" + mChargeId + '\'' +
                ", mPrice=" + mPrice + '\'' +
                '}';
    }

    public boolean isCancelled() {
        return mCancelled;
    }

    public void setCancelled(boolean mCancelled) {
        this.mCancelled = mCancelled;
    }

    public String getChargeId() {   return mChargeId;   }

    public void setChargeId(String chargeId) {  mChargeId = chargeId;   }

    public double getPrice() {   return mPrice;   }

    public void setPrice(double price) {  mPrice = price;   }
}

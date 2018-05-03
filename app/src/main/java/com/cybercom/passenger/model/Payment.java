package com.cybercom.passenger.model;

// Payment status : 0 - success, 1 - pending, 2 - canceled
public class Payment {
    String mDrive;
    String mDriveRequest;
    int mStatus;
    String mPickUp;
    String mDropOff;
    String mAmount;
    String mDistance;

    public String getDrive() {
        return mDrive;
    }

    public void setDrive(String drive) {
        mDrive = drive;
    }

    public String getDriveRequest() {
        return mDriveRequest;
    }

    public void setDriveRequest(String driveRequest) {
        mDriveRequest = driveRequest;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public String getPickUp() {
        return mPickUp;
    }

    public void setPickUp(String pickUp) {
        mPickUp = pickUp;
    }

    public String getDropOff() {
        return mDropOff;
    }

    public void setDropOff(String dropOff) {
        mDropOff = dropOff;
    }

    public String getAmount() {
        return mAmount;
    }

    public void setAmount(String amount) {
        mAmount = amount;
    }

    public String getDistance() {
        return mDistance;
    }

    public void setDistance(String distance) {
        mDistance = distance;
    }

    public Payment(String drive, String driveRequest, int status, String pickUp,
                   String dropOff, String amount, String distance) {
        mDrive = drive;
        mDriveRequest = driveRequest;
        mStatus = status;
        mPickUp = pickUp;
        mDropOff = dropOff;
        mAmount = amount;
        mDistance = distance;
    }

    public Payment()
    {

    }

}

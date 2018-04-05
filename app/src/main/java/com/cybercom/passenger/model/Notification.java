package com.cybercom.passenger.model;

public class Notification {

    public static final int REQUEST_DRIVE = 0;
    public static final int ACCEPT_PASSENGER = 1;
    public static final int REJECT_PASSENGER = 2;

    private int mType;
    private DriveRequest mDriveRequest;
    private Drive mDrive;

    public Notification(int type, DriveRequest driveRequest, Drive drive) {
        mType = type;
        mDriveRequest = driveRequest;
        mDrive = drive;
    }

    public Notification() {
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public DriveRequest getDriveRequest() {
        return mDriveRequest;
    }

    public void setDriveRequest(DriveRequest driveRequest) {
        this.mDriveRequest = driveRequest;
    }

    public Drive getDrive() {
        return mDrive;
    }

    public void setmDrive(Drive mDrive) {
        this.mDrive = mDrive;
    }
}

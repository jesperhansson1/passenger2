package com.cybercom.passenger.repository.databasemodel;

public class Notification {

    public static final int REQUEST_DRIVE = 0;
    public static final int ACCEPT_PASSENGER = 1;
    public static final int REJECT_PASSENGER = 2;

    private int mType;
    private String mDriveRequestId;
    private String mDriveId;

    public Notification(int type, String driveRequestId, String driveId) {
        mType = type;
        mDriveRequestId = driveRequestId;
        mDriveId = driveId;
    }

    public Notification() {
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public String getDriveRequestId() {
        return mDriveRequestId;
    }

    public void setDriveRequestId(String driveRequestId) {
        this.mDriveRequestId = driveRequestId;
    }

    public String getDriveId() {
        return mDriveId;
    }

    public void setDriveId(String driveId) {
        this.mDriveId = driveId;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "mType=" + mType +
                ", mDriveRequestId=" + mDriveRequestId +
                ", mDriveId=" + mDriveId +
                '}';
    }
}

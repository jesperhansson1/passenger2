package com.cybercom.passenger.model;

public class User {

    public static final int TYPE_DRIVER = 0;
    public static final int TYPE_PASSENGER = 1;

    private String mFirstName;
    private String mLastName;
    private String mNotificationTokenId;
    private int mType;

    public User(String firstName, String lastName, String notificationTokenId, int type) {
        this.mFirstName = firstName;
        this.mLastName = lastName;
        this.mNotificationTokenId = notificationTokenId;
        this.mType = type;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String mFirstName) {
        this.mFirstName = mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String mLastName) {
        this.mLastName = mLastName;
    }

    public String getNotificationTokenId() {
        return mNotificationTokenId;
    }

    public void setNotificationTokenId(String mNotificationTokenId) {
        this.mNotificationTokenId = mNotificationTokenId;
    }

    public int getType() {
        return mType;
    }

    public void setType(int mType) {
        this.mType = mType;
    }
}

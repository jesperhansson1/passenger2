package com.cybercom.passenger.model;

public class User {

    public static final int TYPE_DRIVER = 0;
    public static final int TYPE_PASSENGER = 1;

    private String mUserId;
    private String mNotificationTokenId;
    private int mType;
    private String mPhone;
    private String mPassword;
    private String mEmail;
    private String mPersonalNumber;
    private String mFullName;
    private String mImageLink;
    private String mGender;
    
    public User(){

    }

    public User(String userId, String notificationTokenId, int type, String phone, String password,
                String email, String personalNumber, String fullName, String imageLink,
                String gender) {
        mUserId = userId;
        mNotificationTokenId = notificationTokenId;
        mType = type;
        mPhone = phone;
        mPassword = password;
        mEmail = email;
        mPersonalNumber = personalNumber;
        mFullName = fullName;
        mImageLink = imageLink;
        mGender = gender;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public String getEmail() {
        return mEmail;
    }

    @Override
    public String toString() {
        return "User{" +
                "mUserId='" + mUserId + '\'' +
                ", mNotificationTokenId='" + mNotificationTokenId + '\'' +
                ", mType=" + mType +
                ", mPhone='" + mPhone + '\'' +
                ", mPassword='" + mPassword + '\'' +
                ", mEmail='" + mEmail + '\'' +
                ", mPersonalNumber='" + mPersonalNumber + '\'' +
                ", mFullName='" + mFullName + '\'' +
                ", mImageLink='" + mImageLink + '\'' +
                ", mGender='" + mGender + '\'' +
                '}';
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getPersonalNumber() {
        return mPersonalNumber;
    }

    public void setPersonalNumber(String personalNumber) {
        mPersonalNumber = personalNumber;
    }

    public String getFullName() {
        return mFullName;
    }

    public void setFullName(String fullName) {
        mFullName = fullName;
    }

    public String getImageLink() {
        return mImageLink;
    }

    public void setImageLink(String imageLink) {
        mImageLink = imageLink;
    }

    public String getGender() {
        return mGender;
    }

    public void setGender(String gender) {
        mGender = gender;
    }

    public String getNotificationTokenId() {
        return mNotificationTokenId;
    }

    public void setNotificationTokenId(String notificationTokenId) {
        mNotificationTokenId = notificationTokenId;
    }

    public String getUserIdId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

}

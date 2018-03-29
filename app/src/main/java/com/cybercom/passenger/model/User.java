package com.cybercom.passenger.model;

public class User {

    private String mPhone;
    private String mPassword;
    private String mEmail;
    private String mPersonalNumber;
    private String mFullName;
    private String mImageLink;
    private String mGender;
    
    public User(){

    }

    public User(String phone, String password, String email, String personalNumber, String fullName, String imageLink, String gender) {
        this.mPhone = phone;
        this.mPassword = password;
        this.mEmail = email;
        this.mPersonalNumber = personalNumber;
        this.mFullName = fullName;
        this.mImageLink = imageLink;
        this.mGender = gender;
    }

    @Override
    public String toString() {
        return "User{" +
                "mPhone='" + mPhone + '\'' +
                ", mPassword='" + mPassword + '\'' +
                ", mEmail='" + mEmail + '\'' +
                ", mPersonalNumber='" + mPersonalNumber + '\'' +
                ", mFullName='" + mFullName + '\'' +
                ", mImageLink='" + mImageLink + '\'' +
                ", mGender='" + mGender + '\'' +
                '}';
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String phone) {
        this.mPhone = phone;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        this.mPassword = password;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        this.mEmail = email;
    }

    public String getPersonalNumber() {
        return mPersonalNumber;
    }

    public void setPersonalNumber(String personalNumber) {
        this.mPersonalNumber = personalNumber;
    }

    public String getFullName() {
        return mFullName;
    }

    public void setFullName(String fullName) {
        this.mFullName = fullName;
    }

    public String getImageLink() {
        return mImageLink;
    }

    public void setImageLink(String imageLink) {
        this.mImageLink = imageLink;
    }

    public String getGender() {
        return mGender;
    }

    public void setGender(String gender) {
        this.mGender = gender;
    }


    /*

    public static final int TYPE_DRIVER = 0;
    public static final int TYPE_PASSENGER = 1;

    private String mFirstName;
    private String mLastName;
    private String mNotificationTokenId;
    private int mType;

    public User(String firstName, String lastName, String notificationTokenId, int type) {
        mFirstName = firstName;
        mLastName = lastName;
        mNotificationTokenId = notificationTokenId;
        mType = type;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        mLastName = lastName;
    }

    public String getNotificationTokenId() {
        return mNotificationTokenId;
    }

    public void setNotificationTokenId(String notificationTokenId) {
        mNotificationTokenId = notificationTokenId;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    @Override
    public String toString() {
        return "User{" +
                "mFirstName='" + mFirstName + '\'' +
                ", mLastName='" + mLastName + '\'' +
                ", mNotificationTokenId='" + mNotificationTokenId + '\'' +
                ", mType=" + mType +
                '}';
    }*/


}

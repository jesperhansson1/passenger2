package com.cybercom.passenger.model;

import android.os.Parcel;
import android.os.Parcelable;

public class User {

    public static final int TYPE_DRIVER = 0;
    public static final int TYPE_PASSENGER = 1;

    private String mUserId;
    private String mNotificationTokenId;
    private int mType;
    private String mPhone;
    private String mPersonalNumber;
    private String mFullName;
    private String mImageLink;
    private String mGender;
    private String mEmail;
    private String mPassword;
    private String mAccId;

    /*public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    // Parcelling part
    public User(Parcel in){
        mUserId = in.readString();
        mNotificationTokenId = in.readString();
        mType = in.readInt();
        mPhone = in.readString();
        mPersonalNumber = in.readString();
        mFullName = in.readString();
        mImageLink = in.readString();
        mGender = in.readString();
        mEmail = in.readString();
        mPassword = in.readString();
    }
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUserId);
        dest.writeString(mNotificationTokenId);
        dest.writeInt(mType);
        dest.writeString(mPhone);
        dest.writeString(mPersonalNumber);
        dest.writeString(mFullName);
        dest.writeString(mImageLink);
        dest.writeString(mGender);
        dest.writeString(mEmail);
        dest.writeString(mPassword);

    }

*/
    public User(){

    }

    public User(String userId, String notificationTokenId, int type, String phone, String personalNumber, String fullName, String imageLink,
                String gender, String email, String password, String accId) {
        mUserId = userId;
        mNotificationTokenId = notificationTokenId;
        mType = type;
        mPhone = phone;
        mPersonalNumber = personalNumber;
        mFullName = fullName;
        mImageLink = imageLink;
        mGender = gender;
        mEmail = email;
        mPassword = password;
        mAccId = accId;
    }



    public String getmEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    @Override
    public String toString() {
        return "User{" +
                "mUserId='" + mUserId + '\'' +
                ", mNotificationTokenId='" + mNotificationTokenId + '\'' +
                ", mType=" + mType +
                ", mPhone='" + mPhone + '\'' +
                ", mPersonalNumber='" + mPersonalNumber + '\'' +
                ", mFullName='" + mFullName + '\'' +
                ", mImageLink='" + mImageLink + '\'' +
                ", mGender='" + mGender + '\'' +
                ", mAccId='" + mAccId + '\'' +
                '}';
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

    public String getUserId() {
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

    public String getAccId() {
        return mAccId;
    }

    public void setAccId(String accId) {
        mAccId = accId;
    }

}

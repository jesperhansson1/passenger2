package com.cybercom.passenger.model;

import java.io.Serializable;
import java.util.List;

public class DriveRequest implements Serializable {

    private User mPassenger;

    private String mId;
    private long mTime;
    private Position mStartLocation;
    private Position mEndLocation;
    private int mExtraPassengers;
    private List<Object> mDriverIdBlackList;
    private double mPrice;

    public DriveRequest(){
    }

    public DriveRequest(String id, User passenger, long time, Position startLocation, Position endLocation, int extraPassengers, double price, List<Object> driverIdBlackList) {
        mId = id;
        mPassenger = passenger;
        mTime = time;
        mStartLocation = startLocation;
        mEndLocation = endLocation;
        mExtraPassengers = extraPassengers;
        mDriverIdBlackList = driverIdBlackList;
        mPrice = price;
    }

    public User getPassenger() {
        return mPassenger;
    }

    public void setPassenger(User passenger) {
        mPassenger = passenger;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public Position getStartLocation() {
        return mStartLocation;
    }

    public void setStartLocation(Position startLocation) {
        mStartLocation = startLocation;
    }

    public Position getEndLocation() {
        return mEndLocation;
    }

    public void setEndLocation(Position endLocation) {
        mEndLocation = endLocation;
    }

    public int getExtraPassengers() {
        return mExtraPassengers;
    }

    public void setExtraPassengers(int extraPassengers) {
        mExtraPassengers = extraPassengers;
    }

    @Override
    public String toString() {
        return "DriveRequest{" +
                "mId=" + mId +
                ", mTime=" + mTime +
                ", mPassenger=" + mPassenger +
                ", mStartLocation=" + mStartLocation +
                ", mEndLocation=" + mEndLocation +
                ", mExtraPassengers=" + mExtraPassengers +
                ", mPrice=" + mPrice +
                '}';
    }

    public void addDriverIdBlackList(String blackListId) {
        mDriverIdBlackList.add(blackListId);
    }

    public List<Object> getDriverIdBlackList() {
        return mDriverIdBlackList;
    }

    public void setDriverIdBlackList(List<Object> mDriverIdBlackList) {
        this.mDriverIdBlackList = mDriverIdBlackList;
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public double getPrice() {
        return mPrice;
    }

    public void setPrice(double price) {
        mPrice = price;
    }
}

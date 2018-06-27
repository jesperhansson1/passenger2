package com.cybercom.passenger.repository.databasemodel;

import com.cybercom.passenger.model.Position;

import java.io.Serializable;
import java.util.List;

public class DriveRequest implements Serializable {

    private String mPassengerId;

    private long mTime;
    private Position mStartLocation;
    private Position mEndLocation;
    private int mExtraPassengers;
    private List<Object> mDriverIdBlackList;
    private double mPrice;
    private String mChargeId;
    private double mDistance;

    public DriveRequest(){
    }

    public DriveRequest(String passengerId, long time, Position startLocation, Position endLocation,
                        int extraPassengers, double price, String chargeId, List<Object> driverIdBlacklist, double distance) {
        mPassengerId = passengerId;
        mTime = time;
        mStartLocation = startLocation;
        mEndLocation = endLocation;
        mExtraPassengers = extraPassengers;
        mDriverIdBlackList = driverIdBlacklist;
        mPrice = price;
        mChargeId = chargeId;
        mDistance = distance;
    }

    public String getPassengerId() {
        return mPassengerId;
    }

    public void setPassengerId(String passengerId) {
        mPassengerId = passengerId;
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

    public void addDriverIdBlackList(String blackListId) {
        mDriverIdBlackList.add(blackListId);
    }

    @Override
    public String toString() {
        return "DriveRequest{" +
                "mTime=" + mTime +
                ", mPassengerId=" + mPassengerId +
                ", mStartLocation=" + mStartLocation +
                ", mEndLocation=" + mEndLocation +
                ", mExtraPassengers=" + mExtraPassengers +
                ", mPrice=" + mPrice +
                ", mChargeId=" + mChargeId +
                ", mDistance=" + mDistance +
                '}';
    }


    public List<Object> getDriverIdBlackList() {
        return mDriverIdBlackList;
    }

    public void setDriverIdBlackList(List<Object> driverIdBlackList) {
        this.mDriverIdBlackList = driverIdBlackList;
    }

    public double getPrice() {
        return mPrice;
    }

    public void setPrice(double price) {
        mPrice = price;
    }
    public String getChargeId() {
        return mChargeId;
    }

    public void setChargeId(String chargeId) {
        mChargeId = chargeId;
    }

    public double getDistance() {
        return mDistance;
    }

    public void setDistance(double distance) {
        mDistance = distance;
    }
}

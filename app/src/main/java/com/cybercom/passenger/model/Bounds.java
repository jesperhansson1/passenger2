package com.cybercom.passenger.model;

public class Bounds {

    double mNorthEastLatitude;
    double mNorthEastLongitude;
    double mSouthWestLatitude;
    double mSouthWestLongitude;

    Bounds()
    {

    }

    public Bounds(double northEastLatitude, double northEastLongitude, double southWestLatitude, double southWestLongitude) {
        mNorthEastLatitude = northEastLatitude;
        mNorthEastLongitude = northEastLongitude;
        mSouthWestLatitude = southWestLatitude;
        mSouthWestLongitude = southWestLongitude;
    }

    public double getNorthEastLatitude() {
        return mNorthEastLatitude;
    }

    public void setNorthEastLatitude(double northEastLatitude) {
        mNorthEastLatitude = northEastLatitude;
    }

    public double getNorthEastLongitude() {
        return mNorthEastLongitude;
    }

    public void setNorthEastLongitude(double northEastLongitude) {
        mNorthEastLongitude = northEastLongitude;
    }

    public double getSouthWestLatitude() {
        return mSouthWestLatitude;
    }

    public void setSouthWestLatitude(double southWestLatitude) {
        mSouthWestLatitude = southWestLatitude;
    }

    public double getSouthWestLongitude() {
        return mSouthWestLongitude;
    }

    public void setSouthWestLongitude(double southWestLongitude) {
        mSouthWestLongitude = southWestLongitude;
    }

    @Override
    public String toString() {
        return "Bounds{" +
                "NorthEastLatitude=" + mNorthEastLatitude +
                ", NorthEastLongitude=" + mNorthEastLongitude +
                ", SouthWestLatitude=" + mSouthWestLatitude +
                ", SouthWestLongitude=" + mSouthWestLongitude +
                '}';
    }
}

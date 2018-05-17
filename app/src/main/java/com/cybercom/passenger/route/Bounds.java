package com.cybercom.passenger.route;

public class Bounds {

    private final double mMaxLongitude;
    private final double mMinLongitude;
    private final double mMaxLatitude;
    private final double mMinLatitude;

    /**
     * Constructs a new Bounds object
     * @param maxLongitude
     * @param minLongitude
     * @param maxLatitude
     * @param minLatitude
     */
    public Bounds(double maxLongitude, double minLongitude, double maxLatitude,
                  double minLatitude) {
        mMaxLongitude = maxLongitude;
        mMinLongitude = minLongitude;
        mMaxLatitude = maxLatitude;
        mMinLatitude = minLatitude;
    }

    public double getMaximumLongitude() {
        return mMaxLongitude;
    }

    public double getMinimumLongituder() {
        return mMinLongitude;
    }


    public double getMaximumLatitude() {
        return mMaxLatitude;
    }

    public double getMinimumLatitude() {
        return mMinLatitude;
    }
}

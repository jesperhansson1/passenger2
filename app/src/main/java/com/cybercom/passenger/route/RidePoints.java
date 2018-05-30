package com.cybercom.passenger.route;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

public class RidePoints {
    private final LatLng mFromPoint;
    private final LatLng mToPoint;

    /**
     *
     * @param fromPoint
     * @param toPoint
     */
    public RidePoints(@NonNull LatLng fromPoint, @NonNull LatLng toPoint) {
        mFromPoint = fromPoint;
        mToPoint = toPoint;
    }

    /**
     *
     * @return
     */
    @NonNull
    public LatLng getFromPoint() {
        return mFromPoint;
    }

    /**
     *
     * @return
     */
    @NonNull
    public LatLng getToPoint() {
        return mToPoint;
    }
}

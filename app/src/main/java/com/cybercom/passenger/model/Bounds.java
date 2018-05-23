package com.cybercom.passenger.model;

import com.google.android.gms.maps.model.LatLng;

import timber.log.Timber;

import static com.cybercom.passenger.repository.PassengerRepository.DEFAULT_DRIVE_REQUEST_RADIUS;
import static java.lang.Math.*;

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

    public void setNewBounds(int multiplier)
    {
        double dist = getDistance(multiplier);
        Timber.d(mNorthEastLatitude+" : " + mNorthEastLongitude + " : " + mSouthWestLatitude + " : " + mSouthWestLongitude);
        LatLng sw = movePoint(mSouthWestLatitude,mSouthWestLongitude,
                calculateBearing(new LatLng(mNorthEastLatitude,mNorthEastLongitude), new LatLng(mSouthWestLatitude, mSouthWestLongitude)), dist);
        Timber.d( mSouthWestLatitude + " : " + mSouthWestLongitude + " : " + mNorthEastLatitude+" : " + mNorthEastLongitude);
        LatLng ne = movePoint(mNorthEastLatitude,mNorthEastLongitude,calculateBearing(new LatLng(mSouthWestLatitude,mSouthWestLongitude),
                new LatLng(mNorthEastLatitude, mNorthEastLongitude)), dist);
        mNorthEastLatitude = ne.latitude;
        mNorthEastLongitude = ne.longitude;
        mSouthWestLatitude = sw.latitude;
        mSouthWestLongitude = sw.longitude;
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

    public double getDistance(int val)
    {
        int dist = DEFAULT_DRIVE_REQUEST_RADIUS * val;
        return Math.sqrt(2 * dist * dist);
    }

    public LatLng movePoint(double latitude, double longitude, double bearing, double distanceInMetres) {
        //double distanceInMetres = 989.94949366;//700.0;

        double brngRad = toRadians(bearing);
        double latRad = toRadians(latitude);
        double lonRad = toRadians(longitude);
        int earthRadiusInMetres = 6371000;
        double distFrac = distanceInMetres / earthRadiusInMetres;

        double latitudeResult = asin(sin(latRad) * cos(distFrac) + cos(latRad) * sin(distFrac) * cos(brngRad));
        double a = atan2(sin(brngRad) * sin(distFrac) * cos(latRad), cos(distFrac) - sin(latRad) * sin(latitudeResult));
        double longitudeResult = (lonRad + a + 3 * PI) % (2 * PI) - PI;
        Timber.d("latitude in rads: " +latitudeResult + ", longitude in rads: " + longitudeResult);

        Timber.d("latitude: " + toDegrees(latitudeResult) + ", longitude: " + toDegrees(longitudeResult));
        return new LatLng(toDegrees(latitudeResult),toDegrees(longitudeResult));
    }

    private static double calculateBearing(LatLng start, LatLng end) {
        double startLat = Math.toRadians(start.latitude);
        double startLong = Math.toRadians(start.longitude);
        double endLat = Math.toRadians(end.latitude);
        double endLong = Math.toRadians(end.longitude);
        double dLong = endLong - startLong;
        double dPhi = Math.log(Math.tan((endLat / 2.0) + (Math.PI / 4.0)) / Math.tan((startLat / 2.0) + (Math.PI / 4.0)));
        if (Math.abs(dLong) > Math.PI) {
            if (dLong > 0.0) {
                dLong = -(2.0 * Math.PI - dLong);
            } else {
                dLong = (2.0 * Math.PI + dLong);
            }
        }
        double bearing = (Math.toDegrees(Math.atan2(dLong, dPhi)) + 360.0) % 360.0;
        return bearing;
    }
}

package com.cybercom.passenger.model;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

import static com.cybercom.passenger.repository.PassengerRepository.DEFAULT_DRIVE_REQUEST_RADIUS;
import static java.lang.Math.*;

public class Bounds {

    private double mNorthEastLatitude;
    private double mNorthEastLongitude;
    private double mSouthWestLatitude;
    private double mSouthWestLongitude;
    private long mDistance;
    private long mDuration;

    private static final double NORTH_EAST_BEARING = 45.0;
    private static final double SOUTH_WEST_BEARING = 225.0;
    private static final long RADIUS_OF_EARTH = 6371000; // radius of earth in m


    private static final String JSON_NORTH_EAST = "northeast";
    private static final String JSON_SOUTH_WEST = "southwest";
    private static final String JSON_LAT = "lat";
    private static final String JSON_LNG = "lng";

    public static Bounds createInstanceFromJsonObject(final @NonNull JSONObject jsonObjectBounds)
            throws JSONException {
        JSONObject northEast = jsonObjectBounds.getJSONObject(JSON_NORTH_EAST);
        JSONObject southWest = jsonObjectBounds.getJSONObject(JSON_SOUTH_WEST);
        // TODO update with duration and distance..
        return new Bounds(Double.parseDouble(northEast.get(JSON_LAT).toString()),
                Double.parseDouble(northEast.get(JSON_LNG).toString()),
                Double.parseDouble(southWest.get(JSON_LAT).toString()),
                Double.parseDouble(southWest.get(JSON_LNG).toString()), 0, 0);
    }

    public Bounds(double northEastLatitude, double northEastLongitude, double southWestLatitude,
                  double southWestLongitude, long distance, long duration) {
        mNorthEastLatitude = northEastLatitude;
        mNorthEastLongitude = northEastLongitude;
        mSouthWestLatitude = southWestLatitude;
        mSouthWestLongitude = southWestLongitude;
        mDistance = distance;
        mDuration = duration;

    }

    public double getNorthEastLatitude() {
        return mNorthEastLatitude;
    }

    public double getNorthEastLongitude() {
        return mNorthEastLongitude;
    }

    public double getSouthWestLatitude() {
        return mSouthWestLatitude;
    }

    public double getSouthWestLongitude() {
        return mSouthWestLongitude;
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

    private static LatLng movePoint(double latitude, double longitude, double bearing,
                                    double distanceInMetres) {
        //double distanceInMetres = 989.94949366;//700.0;

        double brngRad = toRadians(bearing);
        double latRad = toRadians(latitude);
        double lonRad = toRadians(longitude);
        double distFrac = distanceInMetres / RADIUS_OF_EARTH;

        double latitudeResult = asin(sin(latRad) * cos(distFrac) + cos(latRad) * sin(distFrac)
                * cos(brngRad));
        double a = atan2(sin(brngRad) * sin(distFrac) * cos(latRad), cos(distFrac) - sin(latRad)
                * sin(latitudeResult));
        double longitudeResult = (lonRad + a + 3 * PI) % (2 * PI) - PI;
        Timber.d("latitude in rads: " +latitudeResult + ", longitude in rads: " + longitudeResult);

        Timber.d("latitude: " + toDegrees(latitudeResult) + ", longitude: "
                + toDegrees(longitudeResult));
        return new LatLng(toDegrees(latitudeResult),toDegrees(longitudeResult));
    }

    // TODO remove distance from Bounds
    public long getDistance() {
        return mDistance;
    }

    // TODO remove distance from Bounds
    public void setDistance(long distance) {
        mDistance = distance;
    }

    // TODO remove duration from Bounds
    public long getDuration() {
        return mDuration;
    }

    // TODO remove duration from Bounds
    public void setDuration(long duration) {
        mDuration = duration;
    }

    /**
     * Check if the provided position is within the bounds. The bounds can be extended by prvoiding
     * a non 0 value to the multiplier paramter. This will increase the bounds box by the value
     * of PassengerRepository.DEFAULT_DRIVE_REQUEST_RADIUS time the multiplier.
     * @param bounds
     * @param position
     * @param multiplier
     * @return true, if the position is within the provided bounds, false otherwise
     */
    public static boolean isPositionWithinBounds(@NonNull Bounds bounds, Position position,
                                                 int multiplier) {
        double dist = getDistance(multiplier);

        LatLng sw = movePoint(bounds.getSouthWestLatitude(), bounds.getSouthWestLongitude(),
                SOUTH_WEST_BEARING, dist);

        LatLng ne = movePoint(bounds.getNorthEastLatitude(), bounds.getNorthEastLongitude(),
                NORTH_EAST_BEARING, dist);
        return contains(sw, ne, position.getLatitude(), position.getLongitude());
    }

    private static boolean contains(LatLng sw, LatLng ne, double latitude, double longitude) {
        boolean longitudeContained = false;
        boolean latitudeContained = false;
        double swLongitude = 0.0;
        double swLatitude = 0.0;
        double neLongitude = 0.0;
        double neLatitude = 0.0;

        try {
            swLongitude = sw.longitude;
            swLatitude = sw.latitude;
            neLongitude = ne.longitude;
            neLatitude = ne.latitude;
        } catch (Exception e) {
            Timber.e(e.getLocalizedMessage());
        }

        // Check if the bbox contains the prime meridian (longitude 0.0).
        if (swLongitude < neLongitude) {
            if (swLongitude < longitude && longitude < neLongitude) {
                longitudeContained = true;
            }

        } else if ((0 < longitude && longitude < neLongitude) ||
                (swLongitude < longitude && longitude < 0)) {
            // Contains prime meridian.
            longitudeContained = true;
        }

        if (swLatitude < neLatitude && (swLatitude < latitude && latitude < neLatitude)) {
            latitudeContained = true;
        }
        return (longitudeContained && latitudeContained);
    }

    private static double getDistance(int val) {
        int dist = DEFAULT_DRIVE_REQUEST_RADIUS * val;
        return Math.sqrt(2) * dist;
    }
}

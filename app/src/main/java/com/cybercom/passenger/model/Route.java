package com.cybercom.passenger.model;

import android.support.annotation.NonNull;

import com.cybercom.passenger.route.RouteHelper;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Route {

    private static final String JSON_BOUNDS = "bounds";
    private static final String JSON_COPYRIGHT = "copyright";
    private static final String JSON_LEGS = "legs";
    private static final String JSON_OVERVIEW_POLYLINE = "overview_polyline";
    private static final String JSON_POINTS ="points";

    private long mDistance;
    private long mDuration;
    private Bounds mBounds;
    private List<Leg> mLegs;
    private List<LatLng> mPolyline;

    public static Route createInstanceFromJsonObject(final @NonNull JSONObject jsonObjectRoute)
            throws JSONException {
        return new Route(jsonObjectRoute);
    }

    private Route(final @NonNull JSONObject jsonObjectRoute) throws JSONException {
        JSONArray jsonArrayLegs = jsonObjectRoute.getJSONArray(JSON_LEGS);
        mLegs = new ArrayList<>();
        for(int j=0; j<jsonArrayLegs.length(); j++) {
            JSONObject jsonObjectLeg = (JSONObject)jsonArrayLegs.get(j);
            Leg leg = Leg.createInstanceFromJsonObject(jsonObjectLeg);
            mLegs.add(leg);
        }
        String polylineString =
                (String)((JSONObject)jsonObjectRoute.get(JSON_OVERVIEW_POLYLINE)).get(JSON_POINTS);
        mPolyline = RouteHelper.decodePoly(polylineString);
        mBounds = Bounds.createInstanceFromJsonObject(jsonObjectRoute.getJSONObject(
                JSON_BOUNDS));
        long duration = 0;
        long distance = 0;
        for (Leg leg : mLegs) {
            duration = duration + leg.getDuration().getValue();
            distance = leg.getDistance().getValue();
        }

        //TODO remove duration and distance from Bounds. Implemented like this since Bounce is
        //stored in firebase and is used when matching a passanger with a driver.
        mBounds.setDuration(duration);
        mBounds.setDistance(distance);
        mDuration = duration;
        mDistance = distance;

    }

    public Bounds getBounds() {
        return mBounds;
    }

    @NonNull
    public List<Leg> getLegs() {
        return mLegs;
    }

    public List<LatLng> getPolyline() {
        return mPolyline;
    }


}

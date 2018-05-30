package com.cybercom.passenger.route;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Step {
    //TODO more information can be parsed from this object..
    private static final String JSON_POLYLINE = "polyline";
    private static final String JSON_POINTS ="points";

    private List<LatLng> mPolylinePoints;

    public static Step createInstanceFromJsonObject(final @NonNull JSONObject jsonObjectLeg)
            throws JSONException {
        return new Step(jsonObjectLeg);
    }

    private Step(final @NonNull JSONObject jsonObjectStep) throws JSONException {
        //TODO add "distance", "duration", "end_location", "html_instructions", "travel_mode"
        String polylineString =
                (String)((JSONObject)jsonObjectStep.get(JSON_POLYLINE)).get(JSON_POINTS);
        mPolylinePoints = RouteHelper.decodePoly(polylineString);
    }

    public List<LatLng> getPolylinePointsString() {
        return mPolylinePoints;
    }
}

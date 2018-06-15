package com.cybercom.passenger.model;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Leg {

    private static final String JSON_DISTANCE = "distance";
    private static final String JSON_DURATION = "duration";
    private static final String JSON_START_ADDRESS = "start_address";
    private static final String JSON_END_ADDRESS = "end_address";
    private static final String JSON_START_LOCATION = "start_location";
    private static final String JSON_END_LOCATION = "end_location";
    private static final String JSON_STEPS = "steps";

    private TextValue mDistance;
    private TextValue mDuration;
    private String mStartAddress;
    private String mEndAddress;
    private Position mStartLocation;
    private Position mEndLocation;
    private List<Step> mSteps;

    public static Leg createInstanceFromJsonObject(final @NonNull JSONObject jsonObjectLeg)
            throws JSONException {
        return new Leg(jsonObjectLeg);
    }

    private Leg(final @NonNull JSONObject jsonObjectLeg) throws JSONException {
        mDistance = TextValue.createInstanceFromJsonObject(jsonObjectLeg.getJSONObject(
                JSON_DISTANCE));
        mDuration = TextValue.createInstanceFromJsonObject(jsonObjectLeg.getJSONObject(
                JSON_DURATION));

        mStartAddress = jsonObjectLeg.getString(JSON_START_ADDRESS);
        mEndAddress = jsonObjectLeg.getString(JSON_END_ADDRESS);

        JSONArray jsonArraySteps = jsonObjectLeg.getJSONArray(JSON_STEPS);
        mSteps = new ArrayList<>();
        for(int i=0; i<jsonArraySteps.length(); i++) {
            JSONObject jsonObjectStep = (JSONObject)jsonArraySteps.get(i);
            Step step = Step.createInstanceFromJsonObject(jsonObjectStep);
            mSteps.add(step);
        }
    }

    public List<Step> getSteps() {
        return mSteps;
    }

    public TextValue getDistance() {
        return mDistance;
    }

    public TextValue getDuration() {
        return mDuration;
    }

    public String getStartAddress() {
        return mStartAddress;
    }

    public String getEndAddress() {
        return mEndAddress;
    }
}

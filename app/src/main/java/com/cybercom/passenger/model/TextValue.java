package com.cybercom.passenger.model;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class TextValue {

    private static final String JSON_TEXT = "text";
    private static final String JSON_VALUE = "value";

    private String mText;
    private long mValue;

    public static TextValue createInstanceFromJsonObject(
            final @NonNull JSONObject jsonObjectTextValue) throws JSONException {
        return new TextValue(jsonObjectTextValue);
    }

    private TextValue(final @NonNull JSONObject jsonObjectTextValue) throws JSONException {
        mText = jsonObjectTextValue.getString(JSON_TEXT);
        mValue = jsonObjectTextValue.getLong(JSON_VALUE);
    }

    public String getText() {
        return mText;
    }

    public long getValue() {
        return mValue;
    }
}

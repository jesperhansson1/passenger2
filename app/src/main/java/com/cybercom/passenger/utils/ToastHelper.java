package com.cybercom.passenger.utils;

import android.app.Activity;
import android.widget.Toast;

public class ToastHelper {
    public static Toast makeToast(String message, Activity activity){
        return Toast.makeText(activity, message, Toast.LENGTH_LONG);
    }
}

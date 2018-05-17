package com.cybercom.passenger.route;

import java.util.HashMap;
import java.util.List;

public class Steps {

    List<HashMap<String,String>> mPointsList;

    public Steps() {

    }

    public void setPointsList(List<HashMap<String, String>> pointsList) {
        mPointsList = pointsList;
    }

    public List<HashMap<String,String>> getPointsList() {
        return mPointsList;
    }
}

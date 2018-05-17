package com.cybercom.passenger.route;

import java.util.List;

public class Legs {

    private List<Steps> mSteps;

    public Legs() {

    }

    public void setStepsList(List<Steps> stepsList) {
        mSteps = stepsList;
    }

    public List<Steps> getStepsList() {
        return mSteps;
    }
}

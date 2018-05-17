package com.cybercom.passenger.route;

public class Route {

    private Bounds mBounds;
    private Legs mLegs;

    public Route() {
    }


    public void setBounds(Bounds bounds) {
        mBounds = bounds;
    }

    public Bounds getBounds() {
        return mBounds;
    }

    public void setLegs(Legs legs) {
        mLegs = legs;
    }

    public Legs getLegs() {
        return mLegs;
    }
}

package com.cybercom.passenger.flows.car.infoapi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Basic {

    @SerializedName("data")
    @Expose
    private Data_ data;

    public Data_ getData() {
        return data;
    }

    public void setData(Data_ data) {
        this.data = data;
    }

    public Basic(Data_ data) {
        this.data = data;
    }
}

package com.cybercom.passenger.flows.car.infoapi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Technical {

    @SerializedName("data")
    @Expose
    private Data__ data;

    public Data__ getData() {
        return data;
    }

    public void setData(Data__ data) {
        this.data = data;
    }

}

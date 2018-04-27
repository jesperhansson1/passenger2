package com.cybercom.passenger.flows.car.infoapi;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data1 {

    @SerializedName("type")
    @Expose
    private String type;

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {

        return this.type;
    }

    public Data1(String type) {
        this.type = type;
    }
}

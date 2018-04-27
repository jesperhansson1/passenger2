package com.cybercom.passenger.flows.car.infoapi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Attributes {

    @SerializedName("regno")
    @Expose
    private String regno;
    @SerializedName("vin")
    @Expose
    private String vin;

    public String getRegno() {
        return regno;
    }

    public void setRegno(String regno) {
        this.regno = regno;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public Attributes(String regno, String vin) {
        this.regno = regno;
        this.vin = vin;
    }
}

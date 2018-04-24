package com.cybercom.passenger.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AuthRequest {
    @SerializedName("endUserIp")
    @Expose
    private String endUserIp;

    @SerializedName("requirement")
    @Expose
    private Requirement requirement;

//    public String getEndUserIp() {
//        return endUserIp;
//    }
//
//    public void setEndUserIp(String endUserIp) {
//        this.endUserIp = endUserIp;
//    }

    public AuthRequest(String endUserIp, Requirement requirement) {
        this.endUserIp = endUserIp;
        this.requirement = requirement;
    }
}

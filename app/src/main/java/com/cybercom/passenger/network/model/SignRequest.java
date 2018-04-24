package com.cybercom.passenger.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SignRequest {
    @SerializedName("endUserIp")
    @Expose
    private String endUserIp;
    @SerializedName("userVisibleData")
    @Expose
    private String userVisibleData;


//    public String getEndUserIp() {
//        return endUserIp;
//    }
//
//    public void setEndUserIp(String endUserIp) {
//        this.endUserIp = endUserIp;
//    }

    public SignRequest(String endUserIp, String userVisibleData) {
        this.endUserIp = endUserIp;
        this.userVisibleData = userVisibleData;
    }
}

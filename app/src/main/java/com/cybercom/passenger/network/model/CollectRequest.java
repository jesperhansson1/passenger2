package com.cybercom.passenger.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CollectRequest {
    @SerializedName("orderRef")
    @Expose
    private String orderRef;

//    public String getEndUserIp() {
//        return endUserIp;
//    }
//
//    public void setEndUserIp(String endUserIp) {
//        this.endUserIp = endUserIp;
//    }

    public CollectRequest(String orderRef) {
        this.orderRef = orderRef;
    }
}

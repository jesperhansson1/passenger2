package com.cybercom.passenger.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CollectResponse {

    @SerializedName("orderRef")
    @Expose
    private String orderRef;
    @SerializedName("status")
    @Expose
    private String status;

    @SerializedName("hintCode")
    @Expose
    private String hintCode;

    @SerializedName("completionData")
    @Expose
    private CompletionData completionData;

    public String getOrderRef() {
        return orderRef;
    }

    public void setOrderRef(String orderRef) {
        this.orderRef = orderRef;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHintCode() {
        return hintCode;
    }

    public void setHintCode(String hintCode) {
        this.status = hintCode;
    }

    public CompletionData getCompletionData() {
        return completionData;
    }

    public void setCompletionData(CompletionData completionData) {
        this.completionData = completionData;
    }

}
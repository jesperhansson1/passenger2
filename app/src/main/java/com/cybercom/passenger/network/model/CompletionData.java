package com.cybercom.passenger.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CompletionData {

    @SerializedName("user")
    @Expose
    private User user;
    @SerializedName("device")
    @Expose
    private Device device;
    @SerializedName("cert")
    @Expose
    private Cert cert;
    @SerializedName("signature")
    @Expose
    private String signature;
    @SerializedName("ocspResponse")
    @Expose
    private String ocspResponse;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Cert getCert() {
        return cert;
    }

    public void setCert(Cert cert) {
        this.cert = cert;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getOcspResponse() {
        return ocspResponse;
    }

    public void setOcspResponse(String ocspResponse) {
        this.ocspResponse = ocspResponse;
    }

}

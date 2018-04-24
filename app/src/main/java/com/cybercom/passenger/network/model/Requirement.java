package com.cybercom.passenger.network.model;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Requirement {
    @SerializedName("allowFingerprint")
    @Expose
    private Boolean autoStartTokenRequired;

    public Boolean getautoStartTokenRequired() {
        return autoStartTokenRequired;
    }

    public void setautoStartTokenRequired(Boolean autoStartTokenRequired) {
        this.autoStartTokenRequired = autoStartTokenRequired;
    }

    public Requirement(boolean autoStartTokenRequired) {
        this.autoStartTokenRequired = autoStartTokenRequired;
    }
}

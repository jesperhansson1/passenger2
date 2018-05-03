package com.cybercom.passenger.flows.payment.PriceDistance;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Element
{
    @JsonProperty("distance")
    private Distance mDistance;
    @JsonProperty("duration")
    private Duration mDuration;
    @JsonProperty("status")
    private String mStatus;

    @JsonProperty("distance")
    public Distance getDistance() {
        return mDistance;
    }

    @JsonProperty("duration")
    public Duration getDuration() {
        return mDuration;
    }

    @JsonProperty("status")
    public String getStatus() {
        return mStatus;
    }

}

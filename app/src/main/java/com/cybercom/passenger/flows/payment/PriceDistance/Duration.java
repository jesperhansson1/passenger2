package com.cybercom.passenger.flows.payment.PriceDistance;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Duration
{
    @JsonProperty("text")
    private String mText;
    @JsonProperty("value")
    private Integer mValue;

    @JsonProperty("text")
    public String getText() {
        return mText;
    }

    @JsonProperty("value")
    public Integer getValue() {
        return mValue;
    }
}

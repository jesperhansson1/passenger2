package com.cybercom.passenger.flows.payment.PriceDistance;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class Row {
    @JsonProperty("elements")
    private List<Element> mElement = null;

    @JsonProperty("elements")
    public List<Element> getElements() {
        return mElement;
    }
}

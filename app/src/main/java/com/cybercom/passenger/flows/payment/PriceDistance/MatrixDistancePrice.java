package com.cybercom.passenger.flows.payment.PriceDistance;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatrixDistancePrice {

    @JsonProperty("destination_addresses")
    private List<String> mDestinationAddresses = null;
    @JsonProperty("origin_addresses")
    private List<String> mOriginAddresses = null;
    @JsonProperty("rows")
    private List<Row> mRows = null;
    @JsonProperty("status")
    private String mStatus;

    @JsonProperty("destination_addresses")
    public List<String> getDestinationAddresses() {
        return mDestinationAddresses;
    }

    @JsonProperty("origin_addresses")
    public List<String> getOriginAddresses() {
        return mOriginAddresses;
    }

    @JsonProperty("rows")
    public List<Row> getRows() {
        return mRows;
    }

    @JsonProperty("status")
    public String getStatus() {
        return mStatus;
    }

}

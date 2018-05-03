package com.cybercom.passenger.flows.payment.PriceDistance;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;


public interface APIInterface {
    @GET("maps/api/distancematrix/json")
    Call<MatrixDistancePrice> getDistanceInfo(
            @QueryMap Map<String, String> parameters
    );
}

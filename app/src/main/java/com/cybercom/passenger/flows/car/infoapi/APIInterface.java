package com.cybercom.passenger.flows.car.infoapi;

import retrofit2.Call;

import retrofit2.http.GET;

import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIInterface {

    @Headers("Content-Type: application/json")
    @GET("regno/{regno}")
    Call<Data1> getVehicleDetails(@Path("regno") String regno, @Query("api_token") String api_token);
}

package com.cybercom.passenger.repository.networking;

import com.cybercom.passenger.repository.networking.model.Distance;
import com.cybercom.passenger.repository.networking.model.DistanceMatrixResponse;
import com.cybercom.passenger.repository.networking.model.Duration;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class DistantMatrixAPIHelper {

    private static final String BASE_URL = "https://maps.googleapis.com/";
    private static DistantMatrixAPIHelper INSTANCE;

    public final DistanceMatrixAPIService mMatrixAPIService;


    public static DistantMatrixAPIHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DistantMatrixAPIHelper();
        }
        return INSTANCE;
    }

    private DistantMatrixAPIHelper() {
        mMatrixAPIService = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(DistanceMatrixAPIService.class);
    }

    /**
     * @param response the response
     * @param rowIndex row index
     * @param elementIndex element index
     * @return The (ETA) duration in seconds
     */
    public static long getDurationFromResponse(Response<DistanceMatrixResponse> response,
                                               int rowIndex, int elementIndex) {
        if (response.body() != null) {
            Duration duration = response.body().getRows().get(rowIndex).getElements()
                    .get(elementIndex).getDuration();
            if (duration != null) {
                return duration.getValue();
            }
        }
        return -1;
    }

    /**
     * @param response the response
     * @param rowIndex row index
     * @param elementIndex element index
     * @return The distance in meters
     */
    public static long getDistanceFromResponse(Response<DistanceMatrixResponse> response,
                                               int rowIndex, int elementIndex) {
        if (response.body() != null) {
            Distance distance = response.body().getRows().get(rowIndex).getElements()
                    .get(elementIndex).getDistance();
            if (distance != null) {
                return distance.getValue();
            }
        }
        return -1;
    }

    public interface DistanceMatrixAPIService{
        @GET("maps/api/distancematrix/json")
        Call<DistanceMatrixResponse> getDistantMatrix(@Query("origins") String latAndLongOrigin,
                                                      @Query("destinations") String latAndLongDest,
                                                      @Query("key") String key);
    }

}

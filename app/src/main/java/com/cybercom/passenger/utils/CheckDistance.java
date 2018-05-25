package com.cybercom.passenger.utils;

import com.cybercom.passenger.R;
import com.cybercom.passenger.repository.networking.DistantMatrixAPIHelper;
import com.cybercom.passenger.repository.networking.model.DistanceMatrixResponse;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class CheckDistance {
    public static final long MIN_DURATION = 1800;

    int mPosition = -1;
    public CheckDistance() {

    }

    public int calculateETAToPickUpLocation(String start, LatLng pick, LatLng drop) {
        String pickup = pick.latitude + "," + pick.longitude;
        mPosition = -1;
        DistantMatrixAPIHelper.getInstance().mMatrixAPIService.getDistantMatrix(
                start,
                pickup,
                "AIzaSyDFFhguo8c4JMyaZXtwmcdkvfd33wbjReo").enqueue(new Callback<DistanceMatrixResponse>() {
            @Override
            public void onResponse(Call<DistanceMatrixResponse> call, Response<DistanceMatrixResponse> response) {
                if (response.isSuccessful()) {
                    int size = DistantMatrixAPIHelper.getRowsCount(response);
                    if(size>0)
                    {
                        long minTime = MIN_DURATION;
                        for(int i = 0; i<size; i++)
                        {
                            long l = DistantMatrixAPIHelper.getDurationFromResponse(response, i, 0);
                            Timber.d("position : duration : %s", i + " : " + l + " : " + minTime);
                            if(l<minTime)
                            {
                                minTime = l;
                                mPosition = i;
                                System.out.println(mPosition);
                            }
                            Timber.d("position : duration : %s", i + " : " + l + " : " + minTime);
                        }

                    }


                    /*long duration = DistantMatrixAPIHelper.getDurationFromResponse(response, 0, 0);
                    Timber.d("eta: %s", duration);
                    System.out.println("response : " + response);*/

                }
            }

            @Override
            public void onFailure(Call<DistanceMatrixResponse> call, Throwable t) {
                System.out.println("error");
            }
        });
        return mPosition;
    }
}

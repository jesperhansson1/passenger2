package com.cybercom.passenger.utils;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

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
    MutableLiveData<Integer> pos = new MutableLiveData<>();

    public CheckDistance() {

    }

    public void calculateETAToPickUpLocation(String start, LatLng pick, LatLng drop) {
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
                                System.out.println("position is " + mPosition);
                                break;
                            }
                            Timber.d("position : duration : %s", i + " : " + l + " : " + minTime);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<DistanceMatrixResponse> call, Throwable t) {
                System.out.println("error");
            }
        });
    }
}

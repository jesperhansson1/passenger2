package com.cybercom.passenger.flows.payment;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.cybercom.passenger.flows.payment.PriceDistance.ResultDistanceMatrix;
import com.cybercom.passenger.model.RideFare;
import com.google.android.gms.maps.model.LatLng;

public class PaymentViewModel extends AndroidViewModel {
    public PaymentViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<RideFare> getRideFare(LatLng from, LatLng to){

        ResultDistanceMatrix resultDistanceMatrix = new ResultDistanceMatrix(from,to);
        return resultDistanceMatrix.getRideFare();
    }
}

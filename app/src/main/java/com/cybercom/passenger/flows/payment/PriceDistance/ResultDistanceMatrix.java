package com.cybercom.passenger.flows.payment.PriceDistance;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import com.cybercom.passenger.model.RideFare;
import com.google.android.gms.maps.model.LatLng;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import timber.log.Timber;

import static com.cybercom.passenger.model.ConstantValues.UNIT;

public class ResultDistanceMatrix {
    MutableLiveData<RideFare> mLiveRideFare = new MutableLiveData<RideFare>();
    String mStartLoc,mEndLoc;

    public ResultDistanceMatrix(LatLng startPos, LatLng endPos)
    {
        mStartLoc = String.valueOf(startPos.latitude) + "," + String.valueOf(startPos.longitude);
        mEndLoc = String.valueOf(endPos.latitude) + "," + String.valueOf(endPos.longitude);
        ResultDistance();
    }
    public LiveData<RideFare> getRideFare() {
        return mLiveRideFare;
    }

    public void ResultDistance() {
        // http://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=Washington,DC&destinations=New+York+City,NY
        Map<String, String> mapQuery = new HashMap<>();
        mapQuery.put("units", UNIT);
        mapQuery.put("origins", mStartLoc);
        mapQuery.put("destinations", mEndLoc);

        APIInterface client = RestUtil.getInstance().getRetrofit().create(APIInterface.class);

        Call<MatrixDistancePrice> call = client.getDistanceInfo(mapQuery);
        call.enqueue(new Callback<MatrixDistancePrice>() {
            @Override
            public void onResponse(Call<MatrixDistancePrice> call, retrofit2.Response<MatrixDistancePrice> response) {
                try
                {
                    if (response.body() != null &&
                            response.body().getRows() != null &&
                            response.body().getRows().size() > 0 &&
                            response.body().getRows().get(0) != null &&
                            response.body().getRows().get(0).getElements() != null &&
                            response.body().getRows().get(0).getElements().size() > 0 &&
                            response.body().getRows().get(0).getElements().get(0) != null &&
                            response.body().getRows().get(0).getElements().get(0).getDistance() != null &&
                            response.body().getRows().get(0).getElements().get(0).getDuration() != null) {

                        List<String> dest = response.body().getDestinationAddresses();
                        String destAdd = "";
                        for(int i = 0; i < dest.size(); i++)
                        {
                            destAdd += dest.get(i) + " ";
                        }
                        List<String> sour = response.body().getOriginAddresses();
                        String sourAdd = "";
                        for(int i = 0; i < sour.size(); i++)
                        {
                            sourAdd += sour.get(i) + " ";
                        }

                        Element element = response.body().getRows().get(0).getElements().get(0);
                        String dist = String.valueOf(element.getDistance().getValue());
                        String dura = element.getDuration().getText();
                        Timber.d(element.getDistance().getText() + "\n" + element.getDuration().getText());
                        mLiveRideFare.setValue(new RideFare(sourAdd,destAdd,dist,dura));
                    }
                }
                catch(Exception e)
                {
                    Timber.e(e.getLocalizedMessage());
                }

            }

            @Override
            public void onFailure(Call<MatrixDistancePrice> call, Throwable t) {
                Timber.e(t,call.toString());
            }
        });
    }
}

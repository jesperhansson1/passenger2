package com.cybercom.passenger.flows.car;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import com.cybercom.passenger.model.Car;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

public class CarDetailViewModel extends AndroidViewModel {

    private static final MutableLiveData<Car> mLiveDataCar = new MutableLiveData<>();
    private String mUrl;
    private String mCarNo;

    public CarDetailViewModel(@NonNull Application application) {
        super(application);
    }

    public void setUrl(String url, String carNo)
    {
        mUrl = url;
        mCarNo = carNo;
    }

    public LiveData<Car> getCarLiveData() {
        loadCarDetails();
        return mLiveDataCar;
    }

    private void loadCarDetails(){
        OkHttpHandler okHttpHandler = new OkHttpHandler();
        okHttpHandler.execute(mUrl);
    }

    public class OkHttpHandler extends AsyncTask<String, Void, String> {

        private final OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(String... params) {
            Request.Builder builder = new Request.Builder();
            builder.url(params[0]);
            Request request = builder.build();

            try {
                Response response = client.newCall(request).execute();
                ResponseBody body = response.body();
                if (body != null) {
                    return response.body().string();
                } else {
                    Timber.w("Bode was null");
                }
            } catch (IOException e) {
                Timber.e(e.getLocalizedMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObj = new JSONObject(s);
                Timber.d(jsonObj.toString());
                JSONObject data1 = jsonObj.getJSONObject("data");
                Timber.d(data1.toString());
                JSONObject basic = data1.getJSONObject("basic");
                Timber.d(basic.toString());
                JSONObject data = basic.getJSONObject("data");
                Timber.d(data.toString());
                String model = data.getString("make")+" " +
                        data.getString("model");
                String year = data.getString("model_year");
                String color = data.getString("color");

                mLiveDataCar.setValue(new Car(mCarNo,model,year,color));
            }
            catch(Exception e)
            {
                Timber.e(e.getLocalizedMessage());
            }
        }
    }

}

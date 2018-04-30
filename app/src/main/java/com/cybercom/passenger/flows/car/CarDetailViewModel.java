package com.cybercom.passenger.flows.car;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import com.cybercom.passenger.model.Car;
import org.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class CarDetailViewModel extends AndroidViewModel {

    static MutableLiveData<Car> mLiveDataCar = new MutableLiveData<Car>();
    String mUrl;
    String mCarNo;

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

    public void loadCarDetails(){
        OkHttpHandler okHttpHandler = new OkHttpHandler();
        okHttpHandler.execute(mUrl);
    }

    public class OkHttpHandler extends AsyncTask<String, Void, String> {

        OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(String... params) {
            Request.Builder builder = new Request.Builder();
            builder.url(params[0]);
            Request request = builder.build();

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e) {
                Timber.e(e.getLocalizedMessage());
            }
            return null;
        }

        public void getDetails(String url)
        {
            OkHttpHandler okHttpHandler = new OkHttpHandler();
            okHttpHandler.execute(url);
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

package com.cybercom.passenger.flows.car;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.accounts.AccountActivity;
import com.cybercom.passenger.model.Car;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

import static com.cybercom.passenger.flows.car.CarsActivity.CAR_COLOR;
import static com.cybercom.passenger.flows.car.CarsActivity.CAR_DETAIL;
import static com.cybercom.passenger.flows.car.CarsActivity.CAR_MODEL;
import static com.cybercom.passenger.flows.car.CarsActivity.CAR_NUMBER;
import static com.cybercom.passenger.flows.car.CarsActivity.CAR_YEAR;


public class CarDetailActivity extends AppCompatActivity{

    EditText mEditTextCarNumber,mEditTextCarModel,mEditTextCarYear,mEditTextCarColor;
    Button mButtonSave, mButtonFind;
    Drawable errorDraw;
    Bundle mExtras;
    static final String LOGINARRAY = "loginArray";
    static final String CARARRAY = "carArray";
    ProgressBar progressBar;
    String mApiToken;
    String mApiUrl;

    final String regex = "[A-Za-z]{3}[0-9]{3}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_details);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.add_car);
        initializeUI();
        mExtras = getIntent().getExtras();
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        mApiToken = getResources().getString(R.string.car_api_token);
        mApiUrl = getResources().getString(R.string.car_base_url);
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
        mButtonSave.setText(R.string.next);
    }

    public void initializeUI(){
        mEditTextCarNumber = findViewById(R.id.editText_cardetails_number);
        mEditTextCarModel = findViewById(R.id.editText_cardetails_model);
        mEditTextCarYear = findViewById(R.id.editText_cardetails_year);
        mEditTextCarColor = findViewById(R.id.editText_cardetails_color);
        mEditTextCarModel.setKeyListener(null);
        mEditTextCarYear.setKeyListener(null);
        mEditTextCarColor.setKeyListener(null);

        mButtonSave = findViewById(R.id.button_cardetails_save);
        mButtonFind = findViewById(R.id.button_cardetails_find);
        mButtonFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEditTextCarNumber.getText().toString().isEmpty()){
                    mEditTextCarNumber.setError(getResources().getString(R.string.car_number_error));
                }
                else
                {
                    if(mEditTextCarNumber.getText().toString().matches(regex)){
                        Timber.d("matched");
                        String url = mApiUrl + mEditTextCarNumber.getText().toString() + "?api_token=" + mApiToken;
                        OkHttpHandler okHttpHandler = new OkHttpHandler();
                        okHttpHandler.execute(url);
                    }
                    else
                    {
                        Timber.d("car number didnot match");
                        mEditTextCarNumber.setError(getResources().getString(R.string.car_number_invalid),errorDraw);
                    }

                }
            }
        });
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkError() == 0)
                {
                    progressBar.setVisibility(View.VISIBLE);
                    mButtonSave.setText("");
                    addCarToList();
                }
            }
        });
        errorDraw = new BitmapDrawable(getResources(),
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_error));
    }

    public int checkError(){
        int k = 0;
        if(mEditTextCarNumber.getText().toString().isEmpty()){
            k = 1;
            mEditTextCarNumber.setError(getResources().getString(R.string.car_number_error),errorDraw);
            return k;
        }
        if(mEditTextCarModel.getText().toString().isEmpty()){
            mEditTextCarModel.setError(getResources().getString(R.string.car_model_error),errorDraw);
            k = 2;
            return k;
        }
        if(mEditTextCarYear.getText().toString().isEmpty()){
            mEditTextCarYear.setError(getResources().getString(R.string.car_year_error),errorDraw);
            k = 3;
            return k;
        }
        if(mEditTextCarColor.getText().toString().isEmpty()){
            mEditTextCarColor.setError(getResources().getString(R.string.car_color_error),errorDraw);
            k = 4;
            return k;
        }

        if(Integer.parseInt(mEditTextCarYear.getText().toString()) >
                Calendar.getInstance().get(Calendar.YEAR)){
            mEditTextCarYear.setError(getResources().getString(R.string.car_year_invalid),errorDraw);
            k = 6;
            return k;
        }
        return k;
    }

    public void addCar()
    {
        Intent intent=new Intent();
        intent.putExtra(CAR_NUMBER, mEditTextCarNumber.getText().toString());
        intent.putExtra(CAR_MODEL, mEditTextCarModel.getText().toString());
        intent.putExtra(CAR_YEAR, mEditTextCarYear.getText().toString());
        intent.putExtra(CAR_COLOR,mEditTextCarColor.getText().toString());
        setResult(CAR_DETAIL,intent);
        finish();
    }

    public void addCarToList()
    {
        if(mExtras == null)
        {
            Timber.e("No values found");
        }
        else
        {
            Car newCar = new Car(mEditTextCarNumber.getText().toString(),
                    mEditTextCarModel.getText().toString(),
                    mEditTextCarYear.getText().toString(),
                    mEditTextCarColor.getText().toString());

            Gson gson = new Gson();
            String carArray = gson.toJson(newCar);

            Intent intent=new Intent(getApplicationContext(), AccountActivity.class);
            intent.putExtra(CARARRAY, carArray);
            intent.putExtra(LOGINARRAY, mExtras.getString(LOGINARRAY));
            startActivity(intent);
        }
    }

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mEditTextCarModel.setText(model);
                        mEditTextCarYear.setText(year);
                        mEditTextCarColor.setText(color);
                    }
                });

            }
            catch(Exception e)
            {
                Timber.e(e.getLocalizedMessage());
            }
        }
    }
}

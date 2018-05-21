package com.cybercom.passenger.flows.car;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
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
import java.util.Calendar;
import timber.log.Timber;

import static com.cybercom.passenger.flows.car.CarsActivity.CAR_COLOR;
import static com.cybercom.passenger.flows.car.CarsActivity.CAR_DETAIL;
import static com.cybercom.passenger.flows.car.CarsActivity.CAR_MODEL;
import static com.cybercom.passenger.flows.car.CarsActivity.CAR_NUMBER;
import static com.cybercom.passenger.flows.car.CarsActivity.CAR_YEAR;


public class CarDetailActivity extends AppCompatActivity {

    private static final String LOGIN_ARRAY = "loginArray";
    private static final String CAR_ARRAY = "carArray";

    private static final String REGEX = "[A-Za-z]{3}[0-9]{3}";

    private EditText mEditTextCarNumber;
    private EditText mEditTextCarModel;
    private EditText mEditTextCarYear;
    private EditText mEditTextCarColor;
    private Button mButtonSave;
    private Drawable mErrorDraw;
    private Bundle mExtras;

    private ProgressBar progressBar;
    private String mApiToken;
    private String mApiUrl;
    private CarDetailViewModel mCarDetailViewModel;
    LifecycleOwner myLife;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_details);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.add_car);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorBlue));

        initializeUI();
        mExtras = getIntent().getExtras();
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        mApiToken = getResources().getString(R.string.car_api_token);
        mApiUrl = getResources().getString(R.string.car_base_url);
        mCarDetailViewModel = ViewModelProviders.of(this).get(CarDetailViewModel.class);
        myLife = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
        mButtonSave.setText(R.string.next);
    }

    private void initializeUI(){
        mEditTextCarNumber = findViewById(R.id.editText_cardetails_number);
        mEditTextCarModel = findViewById(R.id.editText_cardetails_model);
        mEditTextCarYear = findViewById(R.id.editText_cardetails_year);
        mEditTextCarColor = findViewById(R.id.editText_cardetails_color);
        /*mEditTextCarModel.setKeyListener(null);
        mEditTextCarYear.setKeyListener(null);
        mEditTextCarColor.setKeyListener(null);*/

        mButtonSave = findViewById(R.id.button_cardetails_save);
        Button buttonFind = findViewById(R.id.button_cardetails_find);

        buttonFind.setOnClickListener(v -> {
            mEditTextCarModel.setText("");
            mEditTextCarYear.setText("");
            mEditTextCarColor.setText("");
            if (mEditTextCarNumber.getText().toString().isEmpty()) {
                mEditTextCarNumber.setError(getResources().getString(R.string.car_number_error));
            }
            else {
                String url = mApiUrl + mEditTextCarNumber.getText().toString() + "?api_token=" + mApiToken;
                getDetails(url,mEditTextCarNumber.getText().toString());
            }
        });
        mButtonSave.setOnClickListener(v -> {

            if(checkError() == 0) {
                progressBar.setVisibility(View.VISIBLE);
                mButtonSave.setText("");
                addCarToList();
            }
        });
        mErrorDraw = new BitmapDrawable(getResources(),BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_error));
    }

    private int checkError(){
        if (mEditTextCarNumber.getText().length() == 0){
            mEditTextCarNumber.setError(getResources().getString(R.string.car_number_error), mErrorDraw);
            return 1;
        }
        if (mEditTextCarModel.getText().length() == 0){
            mEditTextCarModel.setError(getResources().getString(R.string.car_model_error), mErrorDraw);
            return 2;
        }
        if (mEditTextCarYear.getText().length() == 0){
            mEditTextCarYear.setError(getResources().getString(R.string.car_year_error), mErrorDraw);
            return 3;
        }
        if (mEditTextCarColor.getText().length() == 0){
            mEditTextCarColor.setError(getResources().getString(R.string.car_color_error), mErrorDraw);
            return 4;
        }

        if(Integer.parseInt(mEditTextCarYear.getText().toString()) >
                Calendar.getInstance().get(Calendar.YEAR)) {
            mEditTextCarYear.setError(getResources().getString(R.string.car_year_invalid),
                    mErrorDraw);
            return 6;
        }
        return 0;
    }

    public void addCar() {
        Intent intent=new Intent();
        intent.putExtra(CAR_NUMBER, mEditTextCarNumber.getText().toString());
        intent.putExtra(CAR_MODEL, mEditTextCarModel.getText().toString());
        intent.putExtra(CAR_YEAR, mEditTextCarYear.getText().toString());
        intent.putExtra(CAR_COLOR,mEditTextCarColor.getText().toString());
        setResult(CAR_DETAIL, intent);
        finish();
    }

    private void addCarToList() {
        if (mExtras == null) {
            Timber.e("No values found");
        } else {
            Car newCar = new Car(mEditTextCarNumber.getText().toString(),
                    mEditTextCarModel.getText().toString(),
                    mEditTextCarYear.getText().toString(),
                    mEditTextCarColor.getText().toString());

            Gson gson = new Gson();
            String carArray = gson.toJson(newCar);

            Intent intent = new Intent(getApplicationContext(), AccountActivity.class);
            intent.putExtra(CAR_ARRAY, carArray);
            intent.putExtra(LOGIN_ARRAY, mExtras.getString(LOGIN_ARRAY));
            startActivity(intent);
        }
    }

    public void getDetails(String url, String regno)
    {
        mCarDetailViewModel.setUrl(url, regno);

        mCarDetailViewModel.getCarLiveData().observe(myLife, new Observer<Car>() {

            @Override
            public void onChanged(@Nullable Car car) {
                mEditTextCarYear.setText(car.getYear());
                mEditTextCarModel.setText(car.getModel());
                mEditTextCarColor.setText(car.getColor());
            }
        });
    }
}

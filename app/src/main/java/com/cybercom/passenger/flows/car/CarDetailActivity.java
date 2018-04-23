package com.cybercom.passenger.flows.car;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.accounts.AccountActivity;

import timber.log.Timber;

import static com.cybercom.passenger.flows.car.CarsActivity.CAR_COLOR;
import static com.cybercom.passenger.flows.car.CarsActivity.CAR_DETAIL;
import static com.cybercom.passenger.flows.car.CarsActivity.CAR_MODEL;
import static com.cybercom.passenger.flows.car.CarsActivity.CAR_NUMBER;
import static com.cybercom.passenger.flows.car.CarsActivity.CAR_YEAR;

public class CarDetailActivity extends AppCompatActivity{

    EditText mEditTextCarNumber,mEditTextCarModel,mEditTextCarYear,mEditTextCarColor;
    Button mButtonSave;
    Drawable errorDraw;
    Bundle mExtras;

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

    }

    public void initializeUI(){
        mEditTextCarNumber = findViewById(R.id.editText_cardetails_number);
        mEditTextCarModel = findViewById(R.id.editText_cardetails_model);
        mEditTextCarYear = findViewById(R.id.editText_cardetails_year);
        mEditTextCarColor = findViewById(R.id.editText_cardetails_color);
        mButtonSave = findViewById(R.id.button_cardetails_save);
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkError() == 0)
                {
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
            String[] carArray = new String[]{mEditTextCarNumber.getText().toString(),
                    mEditTextCarModel.getText().toString(),
                    mEditTextCarYear.getText().toString(),
                    mEditTextCarColor.getText().toString()};

            Intent intent=new Intent(getApplicationContext(), AccountActivity.class);
            intent.putExtra("loginArray", carArray);
            intent.putExtra("carArray", mExtras.getStringArray("loginArray"));
            startActivity(intent);
        }
    }
}

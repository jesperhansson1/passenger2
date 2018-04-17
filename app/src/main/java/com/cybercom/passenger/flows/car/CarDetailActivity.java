package com.cybercom.passenger.flows.car;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cybercom.passenger.R;

import timber.log.Timber;

public class CarDetailActivity extends AppCompatActivity{

    EditText mEditTextCarNumber,mEditTextCarModel,mEditTextCarYear,mEditTextCarColor;
    Button mButtonSave;
    final int CAR_DETAIL = 17;
    final String CAR_NUMBER = "NUMBER";
    final String CAR_MODEL = "MODEL";
    final String CAR_YEAR = "YEAR";
    final String CAR_COLOR = "COLOUR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_details);
        initializeUI();
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
                    addCar();
                }
            }
        });
    }

    public int checkError(){
        int k = 0;
        if(mEditTextCarNumber.getText().toString().isEmpty()){
            k = 1;
            showAlert(getResources().getString(R.string.car_number_error));
        }
        if(mEditTextCarModel.getText().toString().isEmpty()){
            showAlert(getResources().getString(R.string.car_model_error));
            k = 2;
        }
        if(mEditTextCarYear.getText().toString().isEmpty()){
            showAlert(getResources().getString(R.string.car_year_error));
            k = 3;
        }
        if(mEditTextCarColor.getText().toString().isEmpty()){
            showAlert(getResources().getString(R.string.car_color_error));
            k = 4;
        }
        return k;
    }

    public void showAlert(String message)
    {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }

    public void addCar()
    {
        Intent intent=new Intent();
        intent.putExtra("CAR_NUMBER",mEditTextCarNumber.getText().toString());
        intent.putExtra("CAR_MODEL",mEditTextCarModel.getText().toString());
        intent.putExtra("CAR_YEAR",mEditTextCarYear.getText().toString());
        intent.putExtra("CAR_COLOR",mEditTextCarColor.getText().toString());
        setResult(CAR_DETAIL,intent);
        finish();
    }
}

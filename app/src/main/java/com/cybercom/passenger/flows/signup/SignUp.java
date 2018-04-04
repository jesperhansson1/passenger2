package com.cybercom.passenger.flows.signup;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RadioButton;
import com.cybercom.passenger.R;


public class SignUp extends AppCompatActivity implements View.OnClickListener {

    RadioButton mRadioButtonMale, mRadioButtonFemale;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.signup_title);

        mRadioButtonMale = findViewById(R.id.maleRadioButton);
        mRadioButtonMale.setOnClickListener(this);
        mRadioButtonFemale = findViewById(R.id.femaleRadioButton);
        mRadioButtonFemale.setOnClickListener(this);

        mRadioButtonMale.setBackgroundColor(getResources().getColor(R.color.colorBlue));
        mRadioButtonMale.setTextColor(getResources().getColor(R.color.colorWhite));
        mRadioButtonFemale.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        mRadioButtonFemale.setTextColor(getResources().getColor(R.color.colorBlue));

        mRadioButtonMale.setButtonDrawable(R.drawable.ic_male_white);
        mRadioButtonFemale.setButtonDrawable(R.drawable.ic_woman_blue);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.maleRadioButton:
                // Do something
                mRadioButtonMale.setBackgroundColor(getResources().getColor(R.color.colorBlue));
                mRadioButtonMale.setTextColor(getResources().getColor(R.color.colorWhite));
                mRadioButtonMale.setButtonDrawable(R.drawable.ic_male_white);

                mRadioButtonFemale.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                mRadioButtonFemale.setTextColor(getResources().getColor(R.color.colorBlue));
                mRadioButtonFemale.setButtonDrawable(R.drawable.ic_woman_blue);
                break;
            case R.id.femaleRadioButton:
                // Do something
                mRadioButtonFemale.setBackgroundColor(getResources().getColor(R.color.colorBlue));
                mRadioButtonFemale.setTextColor(getResources().getColor(R.color.colorWhite));
                mRadioButtonFemale.setButtonDrawable(R.drawable.ic_woman_white);

                mRadioButtonMale.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                mRadioButtonMale.setTextColor(getResources().getColor(R.color.colorBlue));
                mRadioButtonMale.setButtonDrawable(R.drawable.ic_male_blue);
                break;
        }    }
}

package com.cybercom.passenger.flows.signup;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.login.LoginActivity;
import com.cybercom.passenger.flows.main.MainActivity;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.utils.CheckTextFieldHelper;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;

import timber.log.Timber;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    SignUpViewModel mViewModel;
    RadioButton mRadioButtonMale, mRadioButtonFemale;
    Button mNextButton;
    public static EditText mPassword, mEmail, mFullName, mPersonalNumber, mPhone;
    String mSaveRadioButtonAnswer;
    public static Boolean mFilledInTextFields = false;
    private static final String GENDER_MALE = "Male";
    private static final String GENDER_FEMALE = "Female";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.signup_title);

        mViewModel = ViewModelProviders.of(this).get(SignUpViewModel.class);

        mPassword = findViewById(R.id.edittext_signup_password);
        mEmail = findViewById(R.id.edittext_signup_email);
        mFullName = findViewById(R.id.edittext_signup_fullName);
        mPersonalNumber = findViewById(R.id.edittext_signup_personalNumer);
        mPhone = findViewById(R.id.edittext_signup_phone);

        mRadioButtonMale = findViewById(R.id.radiobutton_signup_maleRadioButton);
        mRadioButtonMale.setOnClickListener(this);
        mRadioButtonFemale = findViewById(R.id.radiobutton_signup_femaleRadioButton);
        mRadioButtonFemale.setOnClickListener(this);
        mNextButton = findViewById(R.id.button_signup_next);
        mNextButton.setOnClickListener(this);

        mRadioButtonMale.setBackgroundColor(getResources().getColor(R.color.colorBlue));
        mRadioButtonMale.setTextColor(getResources().getColor(R.color.colorWhite));
        mRadioButtonFemale.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        mRadioButtonFemale.setTextColor(getResources().getColor(R.color.colorBlue));

        mRadioButtonMale.setButtonDrawable(R.drawable.ic_male_white);
        mRadioButtonFemale.setButtonDrawable(R.drawable.ic_woman_blue);

        mSaveRadioButtonAnswer = GENDER_MALE;

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.radiobutton_signup_maleRadioButton:
                // Do something
                mSaveRadioButtonAnswer = GENDER_MALE;
                mRadioButtonMale.setBackgroundColor(getResources().getColor(R.color.colorBlue));
                mRadioButtonMale.setTextColor(getResources().getColor(R.color.colorWhite));
                mRadioButtonMale.setButtonDrawable(R.drawable.ic_male_white);

                mRadioButtonFemale.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                mRadioButtonFemale.setTextColor(getResources().getColor(R.color.colorBlue));
                mRadioButtonFemale.setButtonDrawable(R.drawable.ic_woman_blue);
                break;

            case R.id.radiobutton_signup_femaleRadioButton:
                // Do something
                mSaveRadioButtonAnswer = GENDER_FEMALE;
                mRadioButtonFemale.setBackgroundColor(getResources().getColor(R.color.colorBlue));
                mRadioButtonFemale.setTextColor(getResources().getColor(R.color.colorWhite));
                mRadioButtonFemale.setButtonDrawable(R.drawable.ic_woman_white);

                mRadioButtonMale.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                mRadioButtonMale.setTextColor(getResources().getColor(R.color.colorBlue));
                mRadioButtonMale.setButtonDrawable(R.drawable.ic_male_blue);
                break;

            case R.id.button_signup_next:
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                final String fullName = mFullName.getText().toString();
                final String personalNumber = mPersonalNumber.getText().toString();
                final String phone = mPhone.getText().toString();

                if(CheckTextFieldHelper.checkTextFieldsHelper(email, password, fullName, personalNumber, phone)){
                    mViewModel.createUserWithEmailAndPassword(email, password, this).observe(this, new Observer<FirebaseUser>() {
                        @Override
                        public void onChanged(@Nullable FirebaseUser user) {
                            if(user != null){
                                mViewModel.createUser(user.getUid(), new User(user.getUid(), FirebaseInstanceId.getInstance().getToken(),
                                        User.TYPE_PASSENGER, phone, personalNumber, fullName, null, mSaveRadioButtonAnswer));
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                            } else{
                                Toast.makeText(SignUpActivity.this, getResources().getString(R.string.toast_could_not_create_user),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                break;
        }
    }


}

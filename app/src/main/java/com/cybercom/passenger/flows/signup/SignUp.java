package com.cybercom.passenger.flows.signup;

import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.cybercom.passenger.R;
import com.cybercom.passenger.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import timber.log.Timber;

public class SignUp extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth mAuth;
    SignUpViewModel viewModel;

    RadioButton mRadioButtonMale, mRadioButtonFemale;
    Button mNextButton;
    EditText mPassword, mEmail, mFullName, mPersonalNumber, mPhone;
    String mSaveRadioButtonAnswer;
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

        mAuth = FirebaseAuth.getInstance();

        viewModel = ViewModelProviders.of(this).get(SignUpViewModel.class);

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
                FirebaseUser user = mAuth.getInstance().getCurrentUser();
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                String fullName = mFullName.getText().toString();
                String personalNumber = mPersonalNumber.getText().toString();
                String phone = mPhone.getText().toString();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Timber.d("createUserWithEmail:success");

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Timber.w("createUserWithEmail:failure %s", task.getException());
                                }
                            }
                        });

                if(user != null){
                    viewModel.createUser(user.getUid(), new User("notificationId", User.TYPE_PASSENGER, phone, personalNumber, fullName, null, mSaveRadioButtonAnswer));
                }
                break;
        }
    }
}

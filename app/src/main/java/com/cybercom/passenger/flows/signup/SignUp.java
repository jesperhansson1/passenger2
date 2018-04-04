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


public class SignUp extends AppCompatActivity implements View.OnClickListener {
    FirebaseAuth mAuth;

    SignUpViewModel viewModel;

    RadioButton mRadioButtonMale, mRadioButtonFemale;
    Button mNextButton;
    EditText mPassword, mEmail;

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

        mRadioButtonMale = findViewById(R.id.radiobutton_signup_maleRadioButton);
        mRadioButtonMale.setOnClickListener(this);
        mRadioButtonFemale = findViewById(R.id.radiobutton_signup_femaleRadioButton);
        mRadioButtonFemale.setOnClickListener(this);
        mNextButton = findViewById(R.id.button_signup_next);
        mNextButton.setOnClickListener(this);

        mPassword = findViewById(R.id.edittext_signup_password);
        mEmail = findViewById(R.id.edittext_signup_email);


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
                mRadioButtonMale.setBackgroundColor(getResources().getColor(R.color.colorBlue));
                mRadioButtonMale.setTextColor(getResources().getColor(R.color.colorWhite));
                mRadioButtonMale.setButtonDrawable(R.drawable.ic_male_white);

                mRadioButtonFemale.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                mRadioButtonFemale.setTextColor(getResources().getColor(R.color.colorBlue));
                mRadioButtonFemale.setButtonDrawable(R.drawable.ic_woman_blue);
                break;
            case R.id.radiobutton_signup_femaleRadioButton:
                // Do something
                mRadioButtonFemale.setBackgroundColor(getResources().getColor(R.color.colorBlue));
                mRadioButtonFemale.setTextColor(getResources().getColor(R.color.colorWhite));
                mRadioButtonFemale.setButtonDrawable(R.drawable.ic_woman_white);

                mRadioButtonMale.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                mRadioButtonMale.setTextColor(getResources().getColor(R.color.colorBlue));
                mRadioButtonMale.setButtonDrawable(R.drawable.ic_male_blue);
                break;

            case R.id.button_signup_next:
                /*User user = new User("Hej", "wärlden", "s",0);
                viewModel.createUser(user);*/
                FirebaseUser user = mAuth.getInstance().getCurrentUser();
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("TAG", "createUserWithEmail:success");

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("TAG", "createUserWithEmail:failure", task.getException());
                                }
                            }
                        });


                if(user != null){
                    viewModel.createUser(new User(email, mAuth.getUid(), "s",0));
                }
                break;
        }
    }
}

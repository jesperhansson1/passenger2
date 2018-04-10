package com.cybercom.passenger.flows.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.forgotpassword.ForgotPasswordActivity;
import com.cybercom.passenger.flows.main.MainActivity;
import com.cybercom.passenger.flows.signup.SignUpActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import timber.log.Timber;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth mAuth;
    EditText mEmail, mPassword;
    Button mLogin, mSignup, mForgotPassword;
    Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.login_title);

        mEmail = findViewById(R.id.edittext_loginscreen_email);
        mPassword = findViewById(R.id.edittext_loginscreen_password);
        mLogin = findViewById(R.id.button_loginscreen_login);
        mLogin.setOnClickListener(this);
        mSignup = findViewById(R.id.button_loginscreen_signup);
        mSignup.setOnClickListener(this);
        mForgotPassword = findViewById(R.id.button_loginscreen_forgotpassword);
        mForgotPassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();
        mAuth = FirebaseAuth.getInstance();

        switch (v.getId()) {
            case R.id.button_loginscreen_login:
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter email and password to login",
                            Toast.LENGTH_LONG).show();
                } else {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Timber.d("signInWithEmail:success %s", user);
                                        mIntent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(mIntent);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Timber.d("signInWithEmail:failure %s", task.getException());
                                        Toast.makeText(LoginActivity.this, "You have entered an incorrect email",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                break;
            case R.id.button_loginscreen_signup:
                        mIntent = new Intent(getApplicationContext(), SignUpActivity.class);
                        startActivity(mIntent);
                        break;
                    case R.id.button_loginscreen_forgotpassword:
                        mIntent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                        startActivity(mIntent);
                        break;
        }
    }
}

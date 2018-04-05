package com.cybercom.passenger.flows.login;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import timber.log.Timber;

public class Login extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth mAuth;
    EditText mEmail,mPassword;
    Button mLogin, mSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        mEmail = findViewById(R.id.edittext_loginscreen_email);
        mPassword = findViewById(R.id.edittext_loginscreen_password);
        mLogin = findViewById(R.id.button_loginscreen_login);
        mLogin.setOnClickListener(this);
        mSignup = findViewById(R.id.button_loginscreen_signup);
        mSignup.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();
        mAuth = FirebaseAuth.getInstance();

        switch (v.getId()){
            case R.id.button_loginscreen_login:
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Timber.d("signInWithEmail:success %s", user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Timber.d("signInWithEmail:failure %s", task.getException());
                                    Toast.makeText(Login.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;
            case R.id.button_loginscreen_signup:
                Timber.d("clicked signup");
                /*Intent intent = new Intent(this, Signup.class);
                startActivity(intent);*/
                    break;
        }
    }

}

package com.cybercom.passenger.flows.signup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.login.LoginActivity;

public class PasswordSentActivity extends AppCompatActivity {
    TextView mEmail;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_sent);
        mEmail = findViewById(R.id.textView_passwordsent_email);
        String valueFromForgotPassword = getIntent().getStringExtra("EXTRA_SESSION_EMAIL");

        mEmail.setText(valueFromForgotPassword);

        findViewById(R.id.button_passwordsent_openmailclient).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
               openEmailClient();
            }
        });
    }

    public void openEmailClient()
    {
        intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_APP_EMAIL);
        startActivity(intent);
    }
}

package com.cybercom.passenger.flows.signup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cybercom.passenger.R;

public class PasswordSent extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_sent);
        findViewById(R.id.button_passwordsent_openmailclient).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
               openEmailClient();
            }
        });
    }

    public void openEmailClient()
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_APP_EMAIL);
        startActivity(intent);
    }
}

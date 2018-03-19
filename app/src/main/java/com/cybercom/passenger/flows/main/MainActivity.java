package com.cybercom.passenger.flows.main;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.crashlytics.android.Crashlytics;
import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.createdrive.CreateRideDialog;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Timber.i("First log info");
        Fabric.with(this, new Crashlytics());
        addUI();
    }

    public void addUI(){
        final Switch switchRide = findViewById(R.id.switch_ride);
        final FloatingActionButton floatRide = findViewById(R.id.button_createRide);
        floatRide.setImageResource(R.drawable.passenger);
        switchRide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                if(isChecked){
                    floatRide.setImageResource(R.drawable.driver);
                }
                else
                {
                    floatRide.setImageResource(R.drawable.passenger);
                }
            }
        });
        floatRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 if(switchRide.isChecked()){
                    showCreateDriveDialog();
                }
                else
                {
                    showCreateDriveRequestDialog();
                }
           }
        });
    }

    public void showCreateDriveDialog()
    {
        CreateRideDialog createRideDialog = new CreateRideDialog(MainActivity.this, "Create Drive");
        createRideDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        createRideDialog.show();
    }

    public void showCreateDriveRequestDialog()
    {
        CreateRideDialog createRideDialog = new CreateRideDialog(MainActivity.this, "Request");
        createRideDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        createRideDialog.show();
    }
}

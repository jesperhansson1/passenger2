package com.cybercom.passenger.flows.main;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
        final Switch mSwitchRide = findViewById(R.id.switch_ride);
        final FloatingActionButton mFloatRide = findViewById(R.id.button_createRide);
        mFloatRide.setImageResource(R.drawable.passenger);
        mSwitchRide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                if(isChecked){
                    mFloatRide.setImageResource(R.drawable.driver);
                }
                else
                {
                    mFloatRide.setImageResource(R.drawable.passenger);
                }

            }
        });
        mFloatRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 if(mSwitchRide.isChecked()){
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
        CreateRideDialog mCreateRideDialog = new CreateRideDialog(MainActivity.this, "Create Drive");
        mCreateRideDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        mCreateRideDialog.show();
    }

    public void showCreateDriveRequestDialog()
    {
        CreateRideDialog mCreateRideDialog = new CreateRideDialog(MainActivity.this, "Request");
        mCreateRideDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        mCreateRideDialog.show();
    }
}

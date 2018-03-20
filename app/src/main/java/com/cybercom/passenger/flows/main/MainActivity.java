package com.cybercom.passenger.flows.main;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

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
        changeLabelFontStyle(false);
        final Switch switchRide = findViewById(R.id.switch_ride);
        final FloatingActionButton floatRide = findViewById(R.id.button_createRide);
        floatRide.setImageResource(R.drawable.passenger);
        switchRide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                if(isChecked){
                    floatRide.setImageResource(R.drawable.driver);
                    changeLabelFontStyle(true);
                }
                else
                {
                    floatRide.setImageResource(R.drawable.passenger);
                    changeLabelFontStyle(false);
                }
            }
        });
        floatRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                Fragment fragmentPrevious = getFragmentManager().findFragmentByTag("dialog");
                if (fragmentPrevious != null) {
                    fragmentTransaction.remove(fragmentPrevious);
                }
                fragmentTransaction.addToBackStack(null);

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

    public void changeLabelFontStyle(boolean driverValue)
    {
        if(driverValue){
            ((TextView)findViewById(R.id.label_driver)).setTypeface(Typeface.DEFAULT_BOLD);
            ((TextView)findViewById(R.id.label_passenger)).setTypeface(Typeface.DEFAULT);
        }
        else
        {
            ((TextView)findViewById(R.id.label_passenger)).setTypeface(Typeface.DEFAULT_BOLD);
            ((TextView)findViewById(R.id.label_driver)).setTypeface(Typeface.DEFAULT);
        }
    }

    public void showCreateDriveDialog()
    {
        DialogFragment dialogFragment = CreateRideDialog.newInstance("Create Drive");
        dialogFragment.show(getFragmentManager(),"dialog");
    }

    public void showCreateDriveRequestDialog()
    {
        DialogFragment dialogFragment = CreateRideDialog.newInstance("Request");
        dialogFragment.show(getFragmentManager(),"dialog");
    }
}

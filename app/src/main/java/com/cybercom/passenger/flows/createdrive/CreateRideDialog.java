package com.cybercom.passenger.flows.createdrive;

import android.app.Activity;
import android.app.Dialog;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.model.Drive;
import com.cybercom.passenger.model.DriveRequest;

public class CreateRideDialog extends Dialog implements android.view.View.OnClickListener {

    private Activity mActivityOrigin;
    public Dialog mDialogDrive;
    private String[] mlocationValueArray;
    private Location mStartLocation,mEndLocation;
    private String mRide;
    public CreateRideDialog(Activity mActivity, String mRide) {

        super(mActivity);
        // TODO Auto-generated constructor stub
        this.mActivityOrigin = mActivity;
        this.mRide = mRide;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_drive);

        String[] mlocatonArray = mActivityOrigin.getApplicationContext().getResources().getStringArray(R.array.location_array);
        mlocationValueArray = mActivityOrigin.getApplicationContext().getResources().getStringArray(R.array.location_value);

        Button mButtonCreateDrive = findViewById(R.id.button_ride);
        Button mButtonCancel = findViewById(R.id.button_cancel);
        mButtonCreateDrive.setOnClickListener(this);
        mButtonCancel.setOnClickListener(this);
        mButtonCreateDrive.setText(this.mRide);

        Spinner mSpinnerStartLoc = findViewById(R.id.spinner_startLocation);
        CustomAdapter mcustomAdapterStartLoc=new CustomAdapter(getContext(), mlocatonArray);
        mSpinnerStartLoc.setAdapter(mcustomAdapterStartLoc);
        mSpinnerStartLoc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                mStartLocation = getLocation(mlocationValueArray[position]);
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                mStartLocation = getLocation(mlocationValueArray[0]);
            }
        });

        Spinner mSpinnerEndLoc = findViewById(R.id.spinner_endLocation);
        CustomAdapter mcustomAdapterEndLoc =new CustomAdapter(getContext(), mlocatonArray);
        mSpinnerEndLoc.setAdapter(mcustomAdapterEndLoc);
        mSpinnerEndLoc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                mEndLocation = getLocation(mlocationValueArray[position]);
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                mEndLocation = getLocation(mlocationValueArray[0]);
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_ride:
                createRide();
                break;
            case R.id.button_cancel:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }


    private Location getLocation(String mLocationValue)
    {
        double mLat,mLong;
        String[] mLocArr = mLocationValue.split(",");
        mLat = Double.valueOf(mLocArr[0]);
        mLong = Double.valueOf(mLocArr[1]);
        Location mloc = new Location("mabc");
        mloc.setLatitude(mLat);
        mloc.setLongitude(mLong);
        return mloc;
    }

    private void createRide(){
        Long mcurrentTime = System.currentTimeMillis();
        int mSeatsAvailable = 1;

        if (mRide.matches("Create Drive")){
            Drive mDrive = new Drive(mcurrentTime,mStartLocation,mEndLocation,String.valueOf(mcurrentTime),mSeatsAvailable);
            Toast.makeText(getContext(),"Your drive is created from "+ mDrive.getStartLocation().toString() + " to " + mDrive.getEndLocation().toString(), Toast.LENGTH_LONG).show();
        }
        if(mRide.matches("Request")){
            DriveRequest mDriveRequest = new DriveRequest(mcurrentTime,mStartLocation,mEndLocation,String.valueOf(mcurrentTime),mSeatsAvailable);
            Toast.makeText(getContext(),"Your drive request is created from "+ mDriveRequest.getStartLocation().toString() + " to " + mDriveRequest.getEndLocation().toString(),Toast.LENGTH_LONG).show();
        }
    }

}

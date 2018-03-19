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
import com.cybercom.passenger.model.Position;

public class CreateRideDialog extends Dialog implements android.view.View.OnClickListener {

    private Activity mActivityOrigin;
    public Dialog mDialogDrive;
    private String[] mlocationValueArray;
    private Position mStartLocation,mEndLocation;
    private String mRide;

    public CreateRideDialog(Activity activity, String ride) {
       super(activity);
        this.mActivityOrigin = activity;
        this.mRide = ride;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_drive);

        String[] locatonArray = mActivityOrigin.getApplicationContext().getResources().getStringArray(R.array.location_array);
        mlocationValueArray = mActivityOrigin.getApplicationContext().getResources().getStringArray(R.array.location_value);

        Button buttonCreateDrive = findViewById(R.id.button_ride);
        Button buttonCancel = findViewById(R.id.button_cancel);
        buttonCreateDrive.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
        buttonCreateDrive.setText(this.mRide);

        Spinner spinnerStartLoc = findViewById(R.id.spinner_startLocation);
        CustomAdapter customAdapterStartLoc=new CustomAdapter(getContext(), locatonArray);
        spinnerStartLoc.setAdapter(customAdapterStartLoc);
        spinnerStartLoc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                mStartLocation = getPosition(mlocationValueArray[position]);
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                mStartLocation = getPosition(mlocationValueArray[0]);
            }
        });

        Spinner spinnerEndLoc = findViewById(R.id.spinner_endLocation);
        CustomAdapter customAdapterEndLoc =new CustomAdapter(getContext(), locatonArray);
        spinnerEndLoc.setAdapter(customAdapterEndLoc);
        spinnerEndLoc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                mEndLocation = getPosition(mlocationValueArray[position]);
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                mEndLocation = getPosition(mlocationValueArray[0]);
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


    private Location getLocation(String locationValue)
    {
        double lat,lang;
        String[] locArr = locationValue.split(",");
        lat = Double.valueOf(locArr[0]);
        lang = Double.valueOf(locArr[1]);
        Location loc = new Location("mabc");
        loc.setLatitude(lat);
        loc.setLongitude(lang);
        return loc;
    }

    private Position getPosition(String locationValue)
    {
        double lat,lang;
        String[] locArr = locationValue.split(",");
        lat = Double.valueOf(locArr[0]);
        lang = Double.valueOf(locArr[1]);
        Position position = new Position(lat,lang);
        return position;
    }

    private void createRide(){
        Long currentTimeMillis = System.currentTimeMillis();
        int seats = 1;

        if (mRide.matches("Create Drive")){
            Drive drive = new Drive(currentTimeMillis,mStartLocation,mEndLocation,String.valueOf(currentTimeMillis),seats);
            Toast.makeText(getContext(),"Your drive is created from "+ drive.getStartLocation().toString() + " to " + drive.getEndLocation().toString(), Toast.LENGTH_LONG).show();
        }
        if(mRide.matches("Request")){
            DriveRequest driveRequest = new DriveRequest(currentTimeMillis,mStartLocation,mEndLocation,String.valueOf(currentTimeMillis),seats);
            Toast.makeText(getContext(),"Your drive request is created from "+ driveRequest.getStartLocation().toString() + " to " + driveRequest.getEndLocation().toString(),Toast.LENGTH_LONG).show();
        }
    }

}

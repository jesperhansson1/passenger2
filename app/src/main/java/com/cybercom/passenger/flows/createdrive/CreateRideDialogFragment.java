package com.cybercom.passenger.flows.createdrive;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.cybercom.passenger.R;
import com.cybercom.passenger.model.Position;

public class CreateRideDialogFragment extends DialogFragment{

    public static final String TAG = "CREATE_RIDE_DIALOG";
    public static final int TYPE_RIDE = 0;
    public static final int TYPE_REQUEST = 0;

    private String[] mlocationValueArray;
    private Position mStartLocation,mEndLocation;

    private CreateRideViewModel mCreateRideViewModel;
    private int mType;

    public static CreateRideDialogFragment newInstance(int type) {
        CreateRideDialogFragment createRideDialog = new CreateRideDialogFragment();
        Bundle args = new Bundle();
        args.putInt("type", type);
        createRideDialog.setArguments(args);
        return createRideDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = getArguments().getInt("type");
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_drive, container, false);

        mCreateRideViewModel = ViewModelProviders.of((FragmentActivity) getActivity()).get(CreateRideViewModel.class);

        Button button_drive = view.findViewById(R.id.button_ride);

        if (mType == TYPE_RIDE) {
            button_drive.setText(R.string.create_ride);
        } else {
            button_drive.setText(R.string.create_ride_request);
        }

        button_drive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mCreateRideViewModel.createRide(mType,mStartLocation,mEndLocation);
                dismiss();
            }
        });

        view.findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                dismiss();
            }
        });

        String[] locationArray = view.getContext().getResources().getStringArray(R.array.location_array);
        mlocationValueArray = view.getContext().getResources().getStringArray(R.array.location_value);
        Spinner spinnerStartLoc = view.findViewById(R.id.spinner_startLocation);
        CustomAdapter customAdapterStartLoc=new CustomAdapter(view.getContext(), locationArray);
        spinnerStartLoc.setAdapter(customAdapterStartLoc);
        spinnerStartLoc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                mStartLocation = mCreateRideViewModel.getPosition(mlocationValueArray[position]);
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                mStartLocation = mCreateRideViewModel.getPosition(mlocationValueArray[0]);
            }
        });

        Spinner spinnerEndLoc = view.findViewById(R.id.spinner_endLocation);
        CustomAdapter customAdapterEndLoc =new CustomAdapter(view.getContext(), locationArray);
        spinnerEndLoc.setAdapter(customAdapterEndLoc);
        spinnerEndLoc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                mEndLocation = mCreateRideViewModel.getPosition(mlocationValueArray[position]);
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                mEndLocation = mCreateRideViewModel.getPosition(mlocationValueArray[0]);
            }
        });

        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setGravity(Gravity.BOTTOM);
        }

        return view;
    }

}

package com.cybercom.passenger.flows.createdrive;

import android.app.DialogFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.model.Position;

public class CreateRideDialogFragment extends DialogFragment{

    private CreateRideDialogFragmentListener mCreateRideDialogListener;

    public interface CreateRideDialogFragmentListener {
        void onCreateRide(int type, Position startLocation, Position endLocation);
    }


    public static final String TAG = "CREATE_RIDE_DIALOG";
    public static final int TYPE_RIDE = 0;
    public static final int TYPE_REQUEST = 1;

    private String[] mLocationValueArray;
    private Position mStartLocation,mEndLocation;

    private CreateRideViewModel mCreateRideViewModel;
    private int mType;
    public String mLocation;

    public static CreateRideDialogFragment newInstance(int type, String location) {
        CreateRideDialogFragment createRideDialog = new CreateRideDialogFragment();
        Bundle args = new Bundle();
        args.putInt("type", type);
        args.putString("loc", location);
        createRideDialog.setArguments(args);
        return createRideDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = getArguments().getInt("type");
        mLocation = getArguments().getString("loc");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_drive, container, false);

        mCreateRideViewModel = ViewModelProviders.of((FragmentActivity) getActivity()).get(CreateRideViewModel.class);

        Button button_drive = view.findViewById(R.id.button_ride);

        TextView dialogLabel = view.findViewById(R.id.label_ride);

        if (mType == TYPE_RIDE) {
            button_drive.setText(R.string.create_ride);
            dialogLabel.setText(R.string.dialog_create_drive_header);
        } else {
            button_drive.setText(R.string.create_ride_request);
            dialogLabel.setText(R.string.dialog_request_drive_header);

        }

        button_drive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mCreateRideDialogListener.onCreateRide(mType, mStartLocation, mEndLocation);
                dismiss();
            }
        });

        view.findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                dismiss();
            }
        });

       /* String[] locationArray = view.getContext().getResources().getStringArray(R.array.location_array);
        mLocationValueArray = view.getContext().getResources().getStringArray(R.array.location_value);
        Spinner spinnerStartLoc = view.findViewById(R.id.spinner_startLocation);
        CustomAdapter customAdapterStartLoc=new CustomAdapter(view.getContext(), locationArray);
        spinnerStartLoc.setAdapter(customAdapterStartLoc);
        spinnerStartLoc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                mLocationValueArray[0] = mLocation;
                mStartLocation = LocationHelper.getPositionFromString(mLocationValueArray[position]);
                Timber.d("Spinner value: %s",  mLocationValueArray[position]);
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                mStartLocation = LocationHelper.getPositionFromString(mLocationValueArray[0]);
            }
        });

        Spinner spinnerEndLoc = view.findViewById(R.id.spinner_endLocation);
        CustomAdapter customAdapterEndLoc =new CustomAdapter(view.getContext(), locationArray);
        spinnerEndLoc.setAdapter(customAdapterEndLoc);
        spinnerEndLoc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                mEndLocation = LocationHelper.getPositionFromString(mLocationValueArray[position]);
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                mEndLocation = LocationHelper.getPositionFromString(mLocationValueArray[0]);
            }
        });*/

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            getDialog().getWindow().setGravity(Gravity.BOTTOM);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        } else {
            dismiss();
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof CreateRideDialogFragmentListener) {
            mCreateRideDialogListener = (CreateRideDialogFragmentListener) context;
        } else {
            Toast.makeText(context, R.string.must_implement_passenger_notification_listener,
                    Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mCreateRideDialogListener = null;
    }
}

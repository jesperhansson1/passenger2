package com.cybercom.passenger.flows.dropofffragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cybercom.passenger.R;

public class DriverDropOffFragment extends Fragment {

    public static DriverDropOffFragment newInstance() {

        Bundle args = new Bundle();

        DriverDropOffFragment fragment = new DriverDropOffFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver_drop_off,container,false);
    }
}

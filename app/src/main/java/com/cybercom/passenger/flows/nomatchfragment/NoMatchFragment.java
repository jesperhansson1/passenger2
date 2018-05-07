package com.cybercom.passenger.flows.nomatchfragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cybercom.passenger.R;

public class NoMatchFragment extends Fragment {

    public static NoMatchFragment newInstance() {

        Bundle args = new Bundle();

        NoMatchFragment fragment = new NoMatchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_no_match,container,false);
        return view;
    }
}

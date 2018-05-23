package com.cybercom.passenger.flows.nomatchfragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cybercom.passenger.R;
import com.cybercom.passenger.utils.ToastHelper;

public class NoMatchFragment extends Fragment implements View.OnClickListener {

    public static final int BUTTON_TRY_AGAIN = 1;
    public static final int BUTTON_INCREASE_RADIUS = 2;
    public static final int BUTTON_CANCEL = 3;

    private NoMatchButtonListener mNoMatchButtonListener;

    public interface NoMatchButtonListener {
        void onNoMatchButtonClicked(int type);
    }

    public static NoMatchFragment newInstance() {

        Bundle args = new Bundle();

        NoMatchFragment fragment = new NoMatchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_no_match, container, false);
        Button tryAgain = view.findViewById(R.id.fragment_no_match_try_again_button);
        Button increaseRadius = view.findViewById(R.id.fragment_no_match_increase_radius_button);
        Button cancel = view.findViewById(R.id.fragment_no_match_cancel_button);

        tryAgain.setOnClickListener(this);
        increaseRadius.setOnClickListener(this);
        cancel.setOnClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof NoMatchButtonListener) {
            mNoMatchButtonListener = (NoMatchButtonListener) context;
        } else {
            ToastHelper.makeToast(getString(R.string.no_match_fragment_must_implement_listener),
                    getActivity());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_no_match_try_again_button: {
                mNoMatchButtonListener.onNoMatchButtonClicked(BUTTON_TRY_AGAIN);
                break;
            }
            case R.id.fragment_no_match_increase_radius_button: {
                mNoMatchButtonListener.onNoMatchButtonClicked(BUTTON_INCREASE_RADIUS);
                break;
            }
            case R.id.fragment_no_match_cancel_button: {
                mNoMatchButtonListener.onNoMatchButtonClicked(BUTTON_CANCEL);
                break;
            }
        }
    }
}
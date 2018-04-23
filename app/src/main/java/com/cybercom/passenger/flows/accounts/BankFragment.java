package com.cybercom.passenger.flows.accounts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.main.MainActivity;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.repository.PassengerRepository;

import timber.log.Timber;

public class BankFragment extends Fragment {

    EditText mEditTextName, mEditTextAccount;
    Bundle mExtras;
    PassengerRepository repository = PassengerRepository.getInstance();

    public BankFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public BankFragment(Bundle extras) {
        mExtras = extras;
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bank, container, false);
        mEditTextName = rootView.findViewById(R.id.editText_fragmentbank_fullname);
        mEditTextAccount = rootView.findViewById(R.id.editText_fragmentbank_account);
        //after every four digits, add space
        mEditTextAccount.addTextChangedListener(new FormattingTextWatcher(4));
        rootView.findViewById(R.id.button_fragmentbank_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextClick(v);
            }
        });

        mExtras = getActivity().getIntent().getExtras();
        return rootView;
    }

    public void nextClick(View target){
        if(mEditTextName.getText().toString().isEmpty())
        {
            mEditTextName.setError(getResources().getString(R.string.account_name_error));
        }
        else if(mEditTextAccount.getText().toString().isEmpty())
        {
            mEditTextAccount.setError(getResources().getString(R.string.account_number_error));
        }
        else
        {
            Timber.d(mEditTextName.getText().toString());
            Timber.d(mEditTextAccount.getText().toString());
            System.out.println(mExtras);
            createUserReturnMain(mExtras);
        }
    }

    public void createUserReturnMain(Bundle mExtras)
    {
        /*System.out.println(mExtras.get("email"));
        repository.createUserWithEmailAndPassword(mExtras.getString("email"),
                mExtras.getString("password"),User.TYPE_PASSENGER,
                mExtras.getString("phone"), mExtras.getString("personalnumber"),
                mExtras.getString("fullname"), null,
                mExtras.getString("gender"));*/
        repository.createUserWithEmailAndPassword(mExtras.getStringArray("loginArray"));
        startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
    }
}
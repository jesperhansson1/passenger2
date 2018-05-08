package com.cybercom.passenger.flows.accounts;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.main.MainActivity;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.repository.PassengerRepository;
import com.google.firebase.auth.FirebaseUser;

import timber.log.Timber;

import static com.cybercom.passenger.flows.accounts.AccountActivity.CARARRAY;
import static com.cybercom.passenger.flows.accounts.AccountActivity.LOGINARRAY;

public class BankFragment extends Fragment {

    Button mNext;
    EditText mEditTextName, mEditTextAccount;
    Bundle mExtras;
    PassengerRepository repository = PassengerRepository.getInstance();
    ProgressBar progressBar;

    public BankFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public BankFragment(Bundle extras) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bank, container, false);
        mEditTextName = rootView.findViewById(R.id.editText_fragmentbank_fullname);
        mEditTextAccount = rootView.findViewById(R.id.editText_fragmentbank_account);
        progressBar = rootView.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        //after every four digits, add space
        mEditTextAccount.addTextChangedListener(new FormattingTextWatcher(4));
        mNext = rootView.findViewById(R.id.button_fragmentbank_next);

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextClick(v);
            }
        });

        mExtras = getActivity().getIntent().getExtras();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
            progressBar.setVisibility(View.GONE);
            mNext.setText(R.string.next);
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
            createUserReturnMain();
        }
    }

    public void createUserReturnMain()
    {

        progressBar.setVisibility(View.VISIBLE);
        mNext.setText("");
        if(mExtras != null)
        {
            if(mExtras.getString(CARARRAY) != null)
            {

                repository.createUserAddCar(mExtras.getString(LOGINARRAY), mExtras.getString(CARARRAY)).observe(this, new Observer<FirebaseUser>() {
                    @Override
                    public void onChanged(@Nullable FirebaseUser firebaseUser) {
                        if(firebaseUser!=null)
                        {
                            startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
                        } else{
                            progressBar.setVisibility(View.GONE);
                            mNext.setText(R.string.next);
                            Timber.d("Error, no user found");
                        }
                    }
                });
            }
            else
            {
                repository.createUserWithEmailAndPassword(mExtras.getString(LOGINARRAY)).observe(this, new Observer<FirebaseUser>() {
                    @Override
                    public void onChanged(@Nullable FirebaseUser firebaseUser) {
                        if(firebaseUser!=null)
                        {
                            startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
                        } else{
                            progressBar.setVisibility(View.GONE);
                            mNext.setText(R.string.next);
                            Timber.d("Error, no user found");
                        }
                    }
                });
            }
        }else {
            Timber.e("Nothing to add");
        }

    }

    public void createCustomer()
    {
        new StripeAccount(repository).execute();
    }
}
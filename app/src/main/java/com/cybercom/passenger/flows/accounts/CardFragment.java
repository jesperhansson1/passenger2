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
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.main.MainActivity;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.repository.PassengerRepository;
import com.google.firebase.auth.FirebaseUser;
import com.stripe.android.model.Card;

import timber.log.Timber;

import static com.cybercom.passenger.flows.accounts.AccountActivity.CARARRAY;
import static com.cybercom.passenger.flows.accounts.AccountActivity.LOGINARRAY;

public class CardFragment extends Fragment {

    Button mNext;
    EditText mEditTextCard, mEditTextExpire, mEditTextCode;
    Bundle mExtras;
    PassengerRepository repository = PassengerRepository.getInstance();
    ProgressBar progressBar;

    public CardFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public CardFragment(Bundle extras)
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_card, container, false);
        progressBar = rootView.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        mEditTextCard = rootView.findViewById(R.id.editText_fragmentcard_card);
        //after every four digits, add space
        mEditTextCard.addTextChangedListener(new FormattingTextWatcher(4));
        mEditTextExpire = rootView.findViewById(R.id.editText_fragmentcard_expires);
        mEditTextCode = rootView.findViewById(R.id.editText_fragmentcard_securitycode);
        mNext = rootView.findViewById(R.id.button_fragmentcard_next);
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextCardClick(v);
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

    public void nextCardClick(View target){

        if(mEditTextCard.getText().toString().isEmpty())
        {
            mEditTextCard.setError(getResources().getString(R.string.card_number_error));
        }
        else if(mEditTextExpire.getText().toString().isEmpty())
        {
            mEditTextExpire.setError(getResources().getString(R.string.expiry_error));
        }
        else if(mEditTextCode.getText().toString().isEmpty())
        {
            mEditTextCode.setError(getResources().getString(R.string.code_error));
        }
        else
        {
            Timber.d(mEditTextCard.getText().toString());
            Timber.d(mEditTextExpire.getText().toString());
            Timber.d(mEditTextCode.getText().toString());

            String[] expire;
            int month = 0;
            int year = 2000;

            try
            {
                expire = mEditTextExpire.getText().toString().split("/");
                month = Integer.parseInt(expire[0]);
                year = year + Integer.parseInt(expire[1]);
            }
            catch(Exception e)
            {
                Timber.e(e.getLocalizedMessage());
            }

            Card card = new Card(mEditTextCard.getText().toString().replace(' ','-'),
                    month, year, mEditTextCode.getText().toString());

            if (!card.validateCard()) {
                Timber.e("Card is not valid");
                Toast.makeText(getContext(),"Card is not valid",Toast.LENGTH_LONG).show();
            }
            else
            {
                Timber.e("Card is valid");
                Toast.makeText(getContext(),"Card is valid",Toast.LENGTH_LONG).show();
                createUserReturnMain();
            }
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
                            Timber.d("Error, no user found");
                            progressBar.setVisibility(View.GONE);
                            mNext.setText(R.string.next);
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
}

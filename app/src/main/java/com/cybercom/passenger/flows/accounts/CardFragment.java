package com.cybercom.passenger.flows.accounts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
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
import com.cybercom.passenger.repository.PassengerRepository;
import com.stripe.android.model.Card;

import timber.log.Timber;

import static com.cybercom.passenger.flows.accounts.AccountActivity.CARARRAY;
import static com.cybercom.passenger.flows.accounts.AccountActivity.LOGINARRAY;

public class CardFragment extends Fragment {

    private Button mNext;
    private EditText mEditTextCard;
    private EditText mEditTextExpire;
    private EditText mEditTextCode;
    private Bundle mExtras;
    private PassengerRepository repository = PassengerRepository.getInstance();
    private ProgressBar mProgressBar;

    public CardFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public CardFragment(Bundle extras)
    {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_card, container, false);
        mProgressBar = rootView.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);

        mEditTextCard = rootView.findViewById(R.id.editText_fragmentcard_card);
        //after every four digits, add space
        mEditTextCard.addTextChangedListener(new FormattingTextWatcher(4));
        mEditTextExpire = rootView.findViewById(R.id.editText_fragmentcard_expires);
        mEditTextCode = rootView.findViewById(R.id.editText_fragmentcard_securitycode);
        mNext = rootView.findViewById(R.id.button_fragmentcard_next);
        mNext.setOnClickListener(view -> nextCardClick());
        mExtras = getActivity().getIntent().getExtras();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.GONE);
        mNext.setText(R.string.next);
    }

    private void nextCardClick(){

        if(mEditTextCard.getText().toString().isEmpty()) {
            mEditTextCard.setError(getResources().getString(R.string.card_number_error));
        } else if(mEditTextExpire.getText().toString().isEmpty()) {
            mEditTextExpire.setError(getResources().getString(R.string.expiry_error));
        } else if(mEditTextCode.getText().toString().isEmpty()) {
            mEditTextCode.setError(getResources().getString(R.string.code_error));
        } else {
            Timber.d(mEditTextCard.getText().toString());
            Timber.d(mEditTextExpire.getText().toString());
            Timber.d(mEditTextCode.getText().toString());

            String[] expire;
            int month = 0;
            int year = 2000;

            try {
                expire = mEditTextExpire.getText().toString().split("/");
                month = Integer.parseInt(expire[0]);
                year = year + Integer.parseInt(expire[1]);
            } catch(NumberFormatException e) {
                Timber.e(e.getLocalizedMessage());
            }

            Card card = new Card(mEditTextCard.getText().toString().replace(' ','-'), month, year,
                    mEditTextCode.getText().toString());

            if (!card.validateCard()) {
                Timber.e("CARD is not valid");
                Toast.makeText(getContext(),"CARD is not valid",Toast.LENGTH_LONG).show();
            }
            else {
                Timber.e("CARD is valid");
                Toast.makeText(getContext(),"CARD is valid",Toast.LENGTH_LONG).show();
                createUserReturnMain();
            }
        }
    }

    private void createUserReturnMain() {
        mProgressBar.setVisibility(View.VISIBLE);
        mNext.setText("");
        if(mExtras != null) {
            if (mExtras.getString(CARARRAY) != null) {
                repository.createUserAddCar(mExtras.getString(LOGINARRAY),
                        mExtras.getString(CARARRAY)).observe(this, firebaseUser -> {
                            if (firebaseUser!=null) {
                                startActivity(new Intent(getActivity().getApplicationContext(),
                                        MainActivity.class));
                            } else {
                                Timber.d("Error, no user found");
                                mProgressBar.setVisibility(View.GONE);
                                mNext.setText(R.string.next);
                            }
                        });
            }
            else {
                repository.createUserWithEmailAndPassword(mExtras.getString(LOGINARRAY)).observe(
                        this, firebaseUser -> {
                    if (firebaseUser!=null) {
                        startActivity(new Intent(getActivity().getApplicationContext(),
                                MainActivity.class));
                    } else {
                        mProgressBar.setVisibility(View.GONE);
                        mNext.setText(R.string.next);
                        Timber.d("Error, no user found");
                    }
                });
            }
        } else {
            Timber.e("Nothing to add");
        }
    }
}

package com.cybercom.passenger.flows.accounts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.main.MainActivity;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.repository.PassengerRepository;
import com.stripe.android.model.Card;

import timber.log.Timber;

public class CardFragment extends Fragment {

    EditText mEditTextCard, mEditTextExpire, mEditTextCode;
    Bundle mExtras;
    PassengerRepository repository = PassengerRepository.getInstance();
    public CardFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public CardFragment(Bundle extras)
    {
        mExtras = extras;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_card, container, false);

        mEditTextCard = rootView.findViewById(R.id.editText_fragmentcard_card);
        //after every four digits, add space
        mEditTextCard.addTextChangedListener(new FormattingTextWatcher(4));
        mEditTextExpire = rootView.findViewById(R.id.editText_fragmentcard_expires);
        mEditTextCode = rootView.findViewById(R.id.editText_fragmentcard_securitycode);
        rootView.findViewById(R.id.button_fragmentcard_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextCardClick(v);
            }
        });
        mExtras = getActivity().getIntent().getExtras();
        return rootView;
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
                System.out.println(mExtras);
                createUserReturnMain(mExtras);
            }
        }
    }

    public void createUserReturnMain(Bundle mExtras)
    {
        /*repository.createUserWithEmailAndPassword(mExtras.getString("email"),
                mExtras.getString("password"),User.TYPE_PASSENGER,
                mExtras.getString("phone"), mExtras.getString("personalnumber"),
                mExtras.getString("fullname"), null,
                mExtras.getString("gender"));*/
        repository.createUserWithEmailAndPassword(mExtras.getStringArray("loginArray"));
        startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
    }
}

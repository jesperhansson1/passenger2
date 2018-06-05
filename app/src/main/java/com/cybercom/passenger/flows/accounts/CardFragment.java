package com.cybercom.passenger.flows.accounts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.main.MainActivity;
import com.cybercom.passenger.flows.payment.StripeAccountAsyncTask;
import com.cybercom.passenger.flows.payment.StripeCustomerAsyncTask;
import com.cybercom.passenger.flows.payment.StripeTokenAsyncTask;
import com.cybercom.passenger.model.User;
import com.cybercom.passenger.repository.PassengerRepository;
import com.google.gson.Gson;
import com.stripe.android.model.Card;

import java.util.Calendar;

import timber.log.Timber;

import static android.content.Context.WIFI_SERVICE;
import static com.cybercom.passenger.flows.accounts.AccountActivity.CARARRAY;
import static com.cybercom.passenger.flows.accounts.AccountActivity.LOGINARRAY;
import static com.cybercom.passenger.model.User.TYPE_DRIVER;
import static com.cybercom.passenger.model.User.TYPE_PASSENGER;

public class CardFragment extends Fragment implements
        StripeTokenAsyncTask.OnTokenCreated, StripeCustomerAsyncTask.OnCustomerCreated, StripeAccountAsyncTask.OnAccountCreated{

    private Button mNext;
    private EditText mEditTextCard;
    private EditText mEditTextExpire;
    private EditText mEditTextCode;
    private Bundle mExtras;
    private PassengerRepository repository = PassengerRepository.getInstance();
    private ProgressBar mProgressBar;
    private String mEmail;
    private User mUserLogin;
    private int mDay,mMonth,mYear;
    private String mFirstName,mLastName;
    //0 for driver and 1 for passenger
    private int mType;

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

        if(mExtras!=null)
        {
            mUserLogin = (new Gson()).fromJson( mExtras.getString(LOGINARRAY), User.class);
            mEmail = mUserLogin.getmEmail();
            mType = mUserLogin.getType();
            mLastName = mUserLogin.getFullName();
            mFirstName = mUserLogin.getGender();
            mDay = Integer.parseInt(mUserLogin.getPersonalNumber().substring(4,6));
            mMonth = Integer.parseInt(mUserLogin.getPersonalNumber().substring(2,4));
            mYear = Integer.parseInt(mUserLogin.getPersonalNumber().substring(0,2));

            if((mYear + 2000) > Calendar.getInstance().get(Calendar.YEAR))
            {
                mYear = 1900 + mYear;
            }
            else
            {
                mYear = 2000 + mYear;
            }

        }


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

                new StripeTokenAsyncTask(card,this).execute();

            }
        }
    }

    private void createUserReturnMain(String loginArray) {
        mProgressBar.setVisibility(View.VISIBLE);
        mNext.setText("");
        if(mExtras != null) {
            if (mExtras.getString(CARARRAY) != null) {
                repository.createUserAddCar(loginArray,
                        mExtras.getString(CARARRAY)).observe(this, firebaseUser -> {
                            if (firebaseUser!=null) {
                                Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Timber.d("Error, no user found");
                                mProgressBar.setVisibility(View.GONE);
                                mNext.setText(R.string.next);
                            }
                        });
            }
            else {
                repository.createUserWithEmailAndPassword(loginArray).observe(
                        this, firebaseUser -> {
                    if (firebaseUser!=null) {
                        Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
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

    @Override
    public void updateTokenId(String tokenId) {
        Timber.d("token created with id " + tokenId);
        if(mType == TYPE_DRIVER)
        {
            Timber.d("Driver logging.. create connected stripe account");
            new StripeAccountAsyncTask(mDay,mMonth,mYear,mFirstName,mLastName,tokenId,mEmail,getIpAddress(),this).execute();
        }
        if(mType == TYPE_PASSENGER)
        {
            Timber.d("Passenger logging.. create stripe customer");
            new StripeCustomerAsyncTask(tokenId, mEmail, this).execute();
        }
    }

    @Override
    public void updateCustomerId(String customerId) {
        Timber.d("customer created with id " + customerId);
        mUserLogin.setCustomerId(customerId);

        Gson gson = new Gson();
        String loginArray = gson.toJson(mUserLogin);
        createUserReturnMain(loginArray);
    }

    public void updateAccountId(String accountId)
    {
        Timber.d("account created with id " + accountId);
        mUserLogin.setCustomerId(accountId);
        Gson gson = new Gson();
        String loginArray = gson.toJson(mUserLogin);
        createUserReturnMain(loginArray);
    }

    //gets the ipaddress
    public String getIpAddress()
    {
        String ipAddress = "100.100.100.100";
        try
        {
            WifiManager wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(WIFI_SERVICE);
            ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());

        }
        catch(Exception e)
        {
            Timber.d(e.getLocalizedMessage());
        }
        Timber.d("ipaddess " + ipAddress);
        return ipAddress;
    }
}

package com.cybercom.passenger.flows.accounts;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.cybercom.passenger.R;

import timber.log.Timber;

import static com.cybercom.passenger.flows.accounts.AccountActivity.BANK;
import static com.cybercom.passenger.flows.accounts.AccountActivity.BANKCARD;
import static com.cybercom.passenger.flows.accounts.AccountActivity.CARD;

public class AccountDetail extends AppCompatActivity {
        private ViewPager mPager;

        private ToggleButton mCardToggle;
        private ToggleButton mBankToggle;
        private Bundle mExtras;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_account_detail);
            mCardToggle = findViewById(R.id.toggleButton_activityaccountdetail_card);
            mBankToggle = findViewById(R.id.toggleButton_activityaccountdetail_bank);
            mPager = findViewById(R.id.ViewPager_activityaccountdetail);
            mPager.setAdapter(new ScreenSlidePageAdapter(getSupportFragmentManager(),mExtras));
            mExtras = getIntent().getExtras();

            mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    if(position == 1) {
                        mPager.setCurrentItem(1);
                        bankSelected();
                    } else if(position == 0) {
                        mPager.setCurrentItem(0);
                        cardSelected();
                    }
                }
            });

            if (mExtras == null) {
                Timber.d(" account detail error getting values");
            }
            else {
                String bankCard = mExtras.getString(BANKCARD);
                if (BANK.equalsIgnoreCase(bankCard)) {
                    mPager.setCurrentItem(1);
                    bankSelected();
                }
                if (CARD.equalsIgnoreCase(bankCard)) {
                    mPager.setCurrentItem(0);
                    cardSelected();
                }
            }
        }

        public void cardClicked(View target){
            if (mPager.getCurrentItem() != 0) {
                mPager.setCurrentItem(0);
            }
            cardSelected();
        }

        public void bankClicked(View target){
            if (mPager.getCurrentItem() != 1) {
                mPager.setCurrentItem(1);
            }
            bankSelected();
        }

        private void bankSelected(){
            mCardToggle.setChecked(false);
            mCardToggle.setTextColor(getResources().getColor(R.color.colorBlue));
            mCardToggle.setBackgroundDrawable(getResources().getDrawable(R.drawable.left_corner_border_style));
            mBankToggle.setSelected(true);
            mBankToggle.setTextColor(getResources().getColor(R.color.colorWhite));
            mBankToggle.setBackgroundDrawable(getResources().getDrawable(R.drawable.right_corner_fill));
        }

        private void cardSelected() {
            mBankToggle.setChecked(false);
            mBankToggle.setTextColor(getResources().getColor(R.color.colorBlue));
            mBankToggle.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.right_corner_border_style));
            mCardToggle.setChecked(true);
            mCardToggle.setTextColor(getResources().getColor(R.color.colorWhite));
            mCardToggle.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.left_corner_fill));
        }
    }
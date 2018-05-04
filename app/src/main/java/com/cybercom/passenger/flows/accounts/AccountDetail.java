package com.cybercom.passenger.flows.accounts;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ToggleButton;

import com.cybercom.passenger.R;

import timber.log.Timber;

import static com.cybercom.passenger.flows.accounts.AccountActivity.Bank;
import static com.cybercom.passenger.flows.accounts.AccountActivity.BankCard;
import static com.cybercom.passenger.flows.accounts.AccountActivity.Card;

public class AccountDetail extends AppCompatActivity {
        private ViewPager mPager;

        private ScreenSlidePageAdapter mPagerAdapter;
        ToggleButton mCardToggle,mBankToggle;
        Bundle mExtras;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_account_detail);
            mCardToggle = findViewById(R.id.toggleButton_activityaccountdetail_card);
            mBankToggle = findViewById(R.id.toggleButton_activityaccountdetail_bank);
            mPager = (ViewPager) findViewById(R.id.ViewPager_activityaccountdetail);
            mPagerAdapter = new ScreenSlidePageAdapter(getSupportFragmentManager(),mExtras);
            mPager.setAdapter(mPagerAdapter);
            mExtras = getIntent().getExtras();

            mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    if(position == 1){
                        mPager.setCurrentItem(1);
                        bankSelected();
                    }
                    if(position == 0)
                    {
                        mPager.setCurrentItem(0);
                        cardSelected();
                    }
                }
            });

            if (mExtras == null) {
                Timber.d(" accoutn detail Error getting values");
            }
            else {
                String value1 = mExtras.getString(BankCard);
                if (value1.equalsIgnoreCase(Bank)) {
                    mPager.setCurrentItem(1);
                    bankSelected();
                }
                if (value1.equalsIgnoreCase(Card)) {
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

        public void bankSelected(){
            mCardToggle.setChecked(false);
            mCardToggle.setTextColor(getResources().getColor(R.color.colorBlue));
            mCardToggle.setBackgroundDrawable(getResources().getDrawable(R.drawable.left_corner_border_style));
            mBankToggle.setSelected(true);
            mBankToggle.setTextColor(getResources().getColor(R.color.colorWhite));
            mBankToggle.setBackgroundDrawable(getResources().getDrawable(R.drawable.right_corner_fill));
        }

        public void cardSelected()
        {
            mBankToggle.setChecked(false);
            mBankToggle.setTextColor(getResources().getColor(R.color.colorBlue));
            mBankToggle.setBackgroundDrawable(getResources().getDrawable(R.drawable.right_corner_border_style));
            mCardToggle.setChecked(true);
            mCardToggle.setTextColor(getResources().getColor(R.color.colorWhite));
            mCardToggle.setBackgroundDrawable(getResources().getDrawable(R.drawable.left_corner_fill));
        }
    }
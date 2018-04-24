package com.cybercom.passenger.flows.accounts;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ScreenSlidePageAdapter extends FragmentStatePagerAdapter {

    Bundle mExtras;

    public ScreenSlidePageAdapter(FragmentManager fm, Bundle extras) {
        super(fm);
        mExtras = extras;
    }

    // This determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {
        if (position == 1) {
            return (new BankFragment(mExtras));
        }
        return new CardFragment(mExtras);
    }

    // This determines the number of tabs
    @Override
    public int getCount() {
        return 2;
    }

}
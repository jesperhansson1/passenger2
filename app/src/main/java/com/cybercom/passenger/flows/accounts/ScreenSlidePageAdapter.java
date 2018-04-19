package com.cybercom.passenger.flows.accounts;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ScreenSlidePageAdapter extends FragmentStatePagerAdapter {

    public ScreenSlidePageAdapter(FragmentManager fm) {
        super(fm);
    }

    // This determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {
        if (position == 1) {
            return (new BankFragment());
        }
        return new CardFragment();
    }

    // This determines the number of tabs
    @Override
    public int getCount() {
        return 2;
    }

}
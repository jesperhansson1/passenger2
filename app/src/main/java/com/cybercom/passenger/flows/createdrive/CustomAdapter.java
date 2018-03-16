package com.cybercom.passenger.flows.createdrive;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cybercom.passenger.R;

public class CustomAdapter extends BaseAdapter {
    private Context mcontext;
    private String[] mstringNames;
    private LayoutInflater minflter;

    CustomAdapter(Context applicationContext, String[] stringNames) {
        this.mcontext = applicationContext;
        this.mstringNames = stringNames;
        minflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return mstringNames.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = minflter.inflate(R.layout.custom_spinner_item, null);
        TextView names = view.findViewById(R.id.text_spinner);
        names.setText(mstringNames[i]);
        return view;
    }
}

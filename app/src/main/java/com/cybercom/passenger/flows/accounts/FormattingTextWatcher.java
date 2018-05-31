package com.cybercom.passenger.flows.accounts;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Text watcher for giving "#### #### #### ####" format to edit text.
 */

public class FormattingTextWatcher implements TextWatcher {
    private static final String EMPTY_STRING = "";
    private static final String WHITE_SPACE = " ";
    private String lastSource = EMPTY_STRING;
    private int mCount;

    public FormattingTextWatcher(int count)
    {
        mCount = count;
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        String source = s.toString();
        if (!lastSource.equals(source)) {
            source = source.replace(WHITE_SPACE, EMPTY_STRING);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < source.length(); i++) {
                if (i > 0 && i % mCount == 0) {
                        stringBuilder.append(WHITE_SPACE);
                }
                stringBuilder.append(source.charAt(i));
            }
            lastSource = stringBuilder.toString();
            s.replace(0, s.length(), lastSource);
        }
    }

}
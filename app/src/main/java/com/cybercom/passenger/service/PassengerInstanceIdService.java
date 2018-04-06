package com.cybercom.passenger.service;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import timber.log.Timber;

public class PassengerInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        Timber.d("PassengerInstanceIdService: TokenId = "
                + FirebaseInstanceId.getInstance().getToken());

//        TODO: Send this token to Firebase so we always have a valid token to send notifications to
    }
}

package com.cybercom.passenger.service;

import com.cybercom.passenger.repository.PassengerRepository;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import timber.log.Timber;

public class PassengerInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        Timber.i("TokenId = "
                + FirebaseInstanceId.getInstance().getToken());

        PassengerRepository.getInstance()
                .refreshNotificationTokenId(FirebaseInstanceId.getInstance().getToken());
    }
}

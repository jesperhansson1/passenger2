package com.cybercom.passenger.service;

import com.cybercom.passenger.repository.PassengerRepository;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import timber.log.Timber;

public class PassengerMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().size() > 0) {
            Map<String, String> payload = remoteMessage.getData();
            sendNotificationToRepository(payload);
            Timber.i(remoteMessage.getData().toString());
        }
    }

    private void sendNotificationToRepository(Map<String, String> payload) {
        PassengerRepository.getInstance().setNotification(payload);
    }

}

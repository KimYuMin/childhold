package com.example.yuminkim.childhold.services;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.yuminkim.childhold.util.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class ChildholdFirebaseMessagingService extends FirebaseMessagingService {
    private LocalBroadcastManager broadcastManager;

    @Override
    public void onCreate() {
        super.onCreate();
        broadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("receivedmessage", "receivedmessage");
        Log.d("data", remoteMessage.getData().toString());

        Intent intent = new Intent(Constants.KEY_NOTI_FOR_DRIVER);
        intent.putExtra("custom", remoteMessage.getData().get("custom"));
        intent.putExtra("alert", remoteMessage.getData().get("alert"));
        intent.putExtra("title", remoteMessage.getData().get("title"));
        broadcastManager.sendBroadcast(intent);

    }
}

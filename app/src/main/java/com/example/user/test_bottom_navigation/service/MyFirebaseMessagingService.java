package com.example.user.test_bottom_navigation.service;


import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.example.user.test_bottom_navigation.CustomerDialog;
import com.example.user.test_bottom_navigation.HomePageActivity;
import com.example.user.test_bottom_navigation.LoginActivity;
import com.example.user.test_bottom_navigation.R;
import com.example.user.test_bottom_navigation.UserFragment;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData() != null) {

            sendNotification(Objects.requireNonNull(remoteMessage.getNotification()).getBody());

        }
    }

    private void sendNotification(String messageBody) {//這邊要寫通知
        Intent intent = new Intent(getApplicationContext(), HomePageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent,
                PendingIntent.FLAG_ONE_SHOT);

        long[] pattern = {0,200,500};

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_count)
                .setContentTitle("Life Butler")
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(pattern)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Objects.requireNonNull(notificationManager).notify(0 , notificationBuilder.build());

    }

}

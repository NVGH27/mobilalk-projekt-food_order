package com.example.foodorderapp;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

// Az értesítés küldésére
public class CouponAlarmReceiver extends BroadcastReceiver {

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        // Készíts értesítést
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "coupon_channel")
                .setSmallIcon(R.drawable.ic_favs)
                .setContentTitle("Kupon lejár")
                .setContentText("Ne felejtsd el felhasználni a kuponod!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1001, builder.build());
    }
}




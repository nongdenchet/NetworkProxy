package com.rain.networkproxy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

final class NotificationHandler {

    void execute(@NonNull Context context) {
        createNotificationChannel(context);
        fireNotification(context);
    }

    private void fireNotification(@NonNull Context context) {
        final Intent intent = NetworkProxyActivity.newIntent(context);
        Notification notification = new NotificationCompat.Builder(context, Constants.TAG)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(context.getString(R.string.network_proxy_noti_title))
                .setContentText(context.getString(R.string.network_proxy_noti_description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
                .build();
        NotificationManagerCompat.from(context)
                .notify(Constants.NOTIFICATION_ID, notification);
    }

    @SuppressWarnings("ConstantConditions")
    private void createNotificationChannel(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(
                    Constants.CHANNEL_ID,
                    context.getString(R.string.network_proxy_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            context.getSystemService(NotificationManager.class)
                    .createNotificationChannel(channel);
        }
    }
}

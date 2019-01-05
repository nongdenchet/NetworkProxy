package com.rain.networkproxy.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import com.rain.networkproxy.R;
import com.rain.networkproxy.ui.onboarding.OnboardingActivity;

public final class NotificationHandler {
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "NetworkProxy";

    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHandler(@NonNull Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void execute() {
        notificationManager.notify(NOTIFICATION_ID, createNotification());
    }

    private Notification createNotification() {
        final Intent intent = OnboardingActivity.newIntent(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
            return new Notification.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.network_proxy_ic_shortcut)
                    .setContentTitle(context.getString(R.string.network_proxy_notification_title))
                    .setContentText(context.getString(R.string.network_proxy_notification_description))
                    .setAutoCancel(false)
                    .setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
                    .build();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return new Notification.Builder(context)
                    .setSmallIcon(R.drawable.network_proxy_ic_shortcut)
                    .setContentTitle(context.getString(R.string.network_proxy_notification_title))
                    .setContentText(context.getString(R.string.network_proxy_notification_description))
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setAutoCancel(false)
                    .setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
                    .build();
        } else {
            return new Notification.Builder(context)
                    .setSmallIcon(R.drawable.network_proxy_ic_shortcut)
                    .setContentTitle(context.getString(R.string.network_proxy_notification_title))
                    .setContentText(context.getString(R.string.network_proxy_notification_description))
                    .setAutoCancel(false)
                    .setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
                    .getNotification();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressWarnings("ConstantConditions")
    private void createNotificationChannel() {
        final NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.network_proxy_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        context.getSystemService(NotificationManager.class)
                .createNotificationChannel(channel);
    }
}

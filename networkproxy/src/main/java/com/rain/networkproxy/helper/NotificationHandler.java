package com.rain.networkproxy.helper;

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

import com.rain.networkproxy.R;
import com.rain.networkproxy.ui.onboarding.OnboardingActivity;

public final class NotificationHandler {
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "NetworkProxy";

    private final Context context;

    public NotificationHandler(@NonNull Context context) {
        this.context = context;
    }

    public void execute() {
        createNotificationChannel(context);
        fireNotification(context);
    }

    private void fireNotification(@NonNull Context context) {
        final Intent intent = OnboardingActivity.newIntent(context);
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.network_proxy_ic_shortcut)
                .setContentTitle(context.getString(R.string.network_proxy_notification_title))
                .setContentText(context.getString(R.string.network_proxy_notification_description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, 0))
                .build();
        NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID, notification);
    }

    @SuppressWarnings("ConstantConditions")
    private void createNotificationChannel(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.network_proxy_notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            context.getSystemService(NotificationManager.class)
                    .createNotificationChannel(channel);
        }
    }
}

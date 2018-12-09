package com.rain.networkproxy;

import android.support.annotation.NonNull;

import com.rain.networkproxy.helper.NotificationHandler;

public final class InstanceProvider {
    private static InstanceProvider instance;
    private final NPProcess process = new NPProcess();

    @NonNull
    public static InstanceProvider instance() {
        if (instance == null) {
            synchronized (InstanceProvider.class) {
                if (instance == null) {
                    instance = new InstanceProvider();
                }
            }
        }

        return instance;
    }

    NotificationHandler provideNotificationHandler() {
        return new NotificationHandler();
    }

    NPProcess provideProcess() {
        return process;
    }
}

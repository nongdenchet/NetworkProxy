package com.rain.networkproxy;

import android.support.annotation.NonNull;

import com.rain.networkproxy.helper.NotificationHandler;
import com.rain.networkproxy.internal.Dispatcher;
import com.rain.networkproxy.internal.StateProvider;
import com.rain.networkproxy.model.NPState;

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

    public StateProvider<NPState> provideStateProvider() {
        return process;
    }

    public Dispatcher<NPCommand> provideDispatcher() {
        return process;
    }
}

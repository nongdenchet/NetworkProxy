package com.rain.networkproxy;

import android.content.Context;
import android.support.annotation.NonNull;
import okhttp3.Interceptor;

public final class NetworkProxy {
    private static NetworkProxy instance;
    private final NotificationHandler notificationHandler;

    private NetworkProxy(NotificationHandler notificationHandler) {
        this.notificationHandler = notificationHandler;
    }

    public static void init(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context should not be null");
        }

        instance().initialize(context);
    }

    public static Interceptor interceptor() {
        return instance().getInterceptor();
    }

    private void initialize(@NonNull Context context) {
        notificationHandler.execute(context);
    }

    private Interceptor getInterceptor() {
        return new NetworkProxyInterceptor();
    }

    @NonNull
    static NetworkProxy instance() {
        if (instance == null) {
            synchronized (NetworkProxy.class) {
                if (instance == null) {
                    instance = new NetworkProxy(new NotificationHandler());
                }
            }
        }

        return instance;
    }
}

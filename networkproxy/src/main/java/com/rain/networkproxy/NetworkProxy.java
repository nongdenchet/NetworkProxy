package com.rain.networkproxy;

import android.content.Context;
import android.support.annotation.NonNull;

import okhttp3.Interceptor;

public final class NetworkProxy {
    private static NetworkProxy instance;
    private final InstanceProvider instanceProvider;

    private NetworkProxy(@NonNull InstanceProvider instanceProvider) {
        this.instanceProvider = instanceProvider;
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
        instanceProvider.provideNotificationHandler(context).execute();
        instanceProvider.provideProcess().startProcess();
        instanceProvider.provideBroadcastReceiverProcess(context).execute();
        instanceProvider.provideRequestFilterProcess(context).execute();
    }

    private Interceptor getInterceptor() {
        return new NPInterceptor(instanceProvider.provideProcess());
    }

    @NonNull
    private static NetworkProxy instance() {
        if (instance == null) {
            synchronized (NetworkProxy.class) {
                if (instance == null) {
                    instance = new NetworkProxy(InstanceProvider.instance());
                }
            }
        }

        return instance;
    }
}

package com.rain.networkproxy;

import android.content.Context;
import android.support.annotation.NonNull;
import okhttp3.Interceptor;

import static com.rain.networkproxy.ServerThread.NO_PORT;

public final class NetworkProxy {
    private static volatile NetworkProxy instance;
    private final InstanceProvider instanceProvider;

    private NetworkProxy(@NonNull InstanceProvider instanceProvider) {
        this.instanceProvider = instanceProvider;
    }

    /**
     * Initialize NetworkProxy
     *
     * @param context the application context
     */
    public static void init(Context context) {
        init(context, NO_PORT);
    }

    /**
     * Initialize NetworkProxy
     *
     * @param context the application context
     * @param port the port to start socket server
     */
    public static void init(Context context, int port) {
        if (context == null) {
            throw new IllegalArgumentException("context should not be null");
        }

        instance().initialize(context, port);
    }

    public static Interceptor interceptor() {
        return instance().getInterceptor();
    }

    private void initialize(@NonNull Context context, int port) {
        instanceProvider.provideProcess().startProcess(port);
        instanceProvider.provideBroadcastReceiverProcess().execute();
        if (port == NO_PORT) {
            instanceProvider.provideNotificationHandler(context).execute();
            instanceProvider.provideRequestFilterProcess(context).execute();
        }
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

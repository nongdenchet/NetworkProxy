package com.rain.networkproxy;

import android.content.Context;
import android.support.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

public final class NetworkProxy {
    private static NetworkProxy instance;

    private NetworkProxy() {
        // No-op
    }

    @SuppressWarnings("unused")
    public static void init(Context context) {
        instance().initialize();
    }

    public static Interceptor interceptor() {
        return instance().getInterceptor();
    }

    private void initialize() {
        // No-op
    }

    private Interceptor getInterceptor() {
        return new Interceptor() {
            @NonNull
            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {
                return chain.proceed(chain.request());
            }
        };
    }

    @NonNull
    private static NetworkProxy instance() {
        if (instance == null) {
            synchronized (NetworkProxy.class) {
                if (instance == null) {
                    instance = new NetworkProxy();
                }
            }
        }

        return instance;
    }
}

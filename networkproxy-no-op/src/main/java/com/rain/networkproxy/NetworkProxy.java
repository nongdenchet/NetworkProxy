package com.rain.networkproxy;

import android.content.Context;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

@SuppressWarnings({"NullableProblems", "unused"})
public final class NetworkProxy {
    private NetworkProxy() {
        // No-op
    }

    public static void init(Context context) {
        // No-op
    }

    public static Interceptor interceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                return chain.proceed(chain.request());
            }
        };
    }
}

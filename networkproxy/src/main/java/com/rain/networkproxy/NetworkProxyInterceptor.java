package com.rain.networkproxy;

import android.support.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

final class NetworkProxyInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        return chain.proceed(chain.request());
    }
}

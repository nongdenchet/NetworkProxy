package com.rain.networkproxy.utils;

import android.support.annotation.Nullable;

import io.reactivex.disposables.Disposable;

public final class RxUtils {
    private RxUtils() {}

    public static void dispose(@Nullable Disposable disposable) {
        if (disposable != null) {
            disposable.dispose();
        }
    }
}

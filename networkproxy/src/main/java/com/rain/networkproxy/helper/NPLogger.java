package com.rain.networkproxy.helper;

import android.util.Log;

import com.rain.networkproxy.Constants;

public final class NPLogger {
    private NPLogger() {
    }

    public static void log(String message) {
        Log.d(Constants.TAG, message + ", Thread: " + Thread.currentThread());
    }

    public static void logError(String message, Throwable throwable) {
        Log.e(Constants.TAG, message + ", Thread: " + Thread.currentThread() + ", Error: " + throwable);
    }
}

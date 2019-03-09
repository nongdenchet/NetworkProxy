package com.rain.networkproxy.helper;

import android.support.annotation.Nullable;

import java.io.Closeable;
import java.io.IOException;

public final class StreamUtils {
    private StreamUtils() {}

    public static void close(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                NPLogger.logError("StreamUtils#close", e);
            }
        }
    }
}

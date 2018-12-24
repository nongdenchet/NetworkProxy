package com.rain.networkproxy.ui;

import java.io.IOException;
import java.nio.charset.Charset;

import android.os.Build;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;

public final class Utils {
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private Utils() {}

    public static DisplayMetrics getScreenSize(WindowManager windowManager) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    public static int getOverlayType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        return WindowManager.LayoutParams.TYPE_PHONE;
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public static String readFromBuffer(Headers headers, @NonNull ResponseBody responseBody) throws IOException {
        long contentLength = responseBody.contentLength();
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE);
        Buffer buffer = source.buffer();

        if ("gzip".equalsIgnoreCase(headers.get("Content-Encoding"))) {
            GzipSource gzippedResponseBody = null;
            try {
                gzippedResponseBody = new GzipSource(buffer.clone());
                buffer = new Buffer();
                buffer.writeAll(gzippedResponseBody);
            } finally {
                if (gzippedResponseBody != null) {
                    gzippedResponseBody.close();
                }
            }
        }

        Charset charset = UTF8;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            charset = contentType.charset(UTF8);
        }
        if (charset == null) {
            charset = UTF8;
        }

        if (contentLength != 0) {
            return buffer.clone().readString(charset);
        }

        return "{}";
    }
}

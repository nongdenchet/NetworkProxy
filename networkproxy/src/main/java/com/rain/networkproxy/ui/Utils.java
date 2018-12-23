package com.rain.networkproxy.ui;

import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

final class Utils {

    private Utils() {}

    static DisplayMetrics getScreenSize(WindowManager windowManager) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    static int getOverlayType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        return WindowManager.LayoutParams.TYPE_PHONE;
    }
}

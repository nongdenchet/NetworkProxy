package com.rain.networkproxy.ui.dashboard;

import com.rain.networkproxy.R;
import com.rain.networkproxy.helper.NPLogger;
import com.rain.networkproxy.ui.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

final class RemoveBar {
    private final Context context;
    private final WindowManager windowManager;
    private final int height;

    @Nullable
    private FrameLayout contentView;

    RemoveBar(@NonNull Context context) {
        this.context = context;
        this.height = context.getResources().getDimensionPixelSize(R.dimen.network_proxy_remove_bar_height);
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    @SuppressLint("InflateParams")
    @NonNull
    private FrameLayout getContentView() {
        if (contentView == null) {
            FrameLayout contentView = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.network_proxy_remove_bar, null);
            contentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
            this.contentView = contentView;
        }
        return contentView;
    }

    void update(float y) {
        final int color = y > getY() - height ? R.color.network_proxy_red : R.color.network_proxy_light_red;
        getContentView().setBackgroundColor(ContextCompat.getColor(context,color));
    }

    void attach() {
        final FrameLayout contentView = getContentView();
        if (!ViewCompat.isAttachedToWindow(contentView)) {
            windowManager.addView(contentView, getLayoutParams(contentView));
        }
    }

    void detach() {
        try {
            windowManager.removeView(getContentView());
        } catch (IllegalArgumentException e) {
            NPLogger.logError("RemoveBar#attach", e);
        }
    }

    int getY() {
        return Utils.getScreenSize(windowManager).heightPixels - height;
    }

    int getHeight() {
        return height;
    }

    private WindowManager.LayoutParams getLayoutParams(@NonNull FrameLayout window) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.format = PixelFormat.RGBA_8888;
        params.type = Utils.getOverlayType();
        params.gravity = Gravity.TOP | Gravity.START;
        params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.width = window.getLayoutParams().width;
        params.height = window.getLayoutParams().height;
        params.y = Utils.getScreenSize(windowManager).heightPixels - height;
        params.x = 0;
        return params;
    }
}

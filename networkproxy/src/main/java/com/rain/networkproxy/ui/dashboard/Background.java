package com.rain.networkproxy.ui.dashboard;

import com.rain.networkproxy.helper.NPLogger;
import com.rain.networkproxy.ui.Utils;

import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

final class Background {
    private final Context context;
    private final WindowManager windowManager;

    @Nullable
    private FrameLayout contentView;

    Background(@NonNull Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    @NonNull
    private FrameLayout getContentView() {
        if (contentView == null) {
            contentView = new FrameLayout(context);
        }
        return contentView;
    }

    private WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.format = PixelFormat.RGBA_8888;
        params.type = Utils.getOverlayType();
        params.gravity = Gravity.TOP | Gravity.START;
        params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.width = 0;
        params.height = 0;
        return params;
    }

    void setOnClickListener(final View.OnClickListener onClickListener) {
        getContentView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(v);
            }
        });
    }

    void deactivateBackground() {
        final FrameLayout contentView = getContentView();
        final WindowManager.LayoutParams params = (WindowManager.LayoutParams) contentView.getLayoutParams();
        params.width = 0;
        params.height = 0;
        windowManager.updateViewLayout(contentView, params);
    }

    void activateBackground() {
        final FrameLayout contentView = getContentView();
        final WindowManager.LayoutParams params = (WindowManager.LayoutParams) contentView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        windowManager.updateViewLayout(contentView, params);
    }

    void attach() {
        final FrameLayout contentView = getContentView();
        if (!Utils.isAttachedToWindow(contentView)) {
            windowManager.addView(contentView, getLayoutParams());
        }
    }

    void detach() {
        try {
            windowManager.removeView(getContentView());
        } catch (IllegalArgumentException e) {
            NPLogger.logError("Background#dtach", e);
        }
    }
}

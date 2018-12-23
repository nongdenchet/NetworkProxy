package com.rain.networkproxy.ui;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

abstract class OverlayService extends Service implements View.OnTouchListener {
    private static final long MAX_CLICK_DURATION = 100;

    private WindowManager windowManager;
    private FrameLayout window;
    private boolean moving = false;
    private long startClickTime = 0;

    abstract View onCreateView(ViewGroup window);

    abstract void onViewCreated(View view);

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        // Initialize window
        window = new FrameLayout(this) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && onBackPressed()) {
                    return true;
                }
                return super.dispatchKeyEvent(event);
            }
        };
        window.setLayoutParams(new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        window.addView(onCreateView(window));
        window.setFocusable(true);

        // Initialize layout params
        Point position = getInitPosition();
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.format = PixelFormat.RGBA_8888;
        params.type = Utils.getOverlayType();
        params.gravity = Gravity.TOP | Gravity.START;
        params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.width = window.getLayoutParams().width;
        params.height = window.getLayoutParams().height;
        params.x = position.x;
        params.y = position.y;
        windowManager.addView(window, params);
        onViewCreated(window);
    }

    protected void unFocusWindow() {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) window.getLayoutParams();
        params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        windowManager.updateViewLayout(window, params);
    }

    protected void focusWindow() {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) window.getLayoutParams();
        params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        windowManager.updateViewLayout(window, params);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final float x = event.getRawX();
        final float y = event.getRawY();

        if (event.getAction() == ACTION_DOWN) {
            startClickTime = System.currentTimeMillis();
        } else if (event.getAction() == ACTION_MOVE) {
            if (!moving && (System.currentTimeMillis() - startClickTime > MAX_CLICK_DURATION)) {
                onDragStarted(x, y);
                moving = true;
            }

            if (!moving) {
                return false;
            }

            WindowManager.LayoutParams params = (WindowManager.LayoutParams) window.getLayoutParams();
            params.x = (int) (x - view.getWidth());
            params.y = (int) (y - view.getHeight());
            windowManager.updateViewLayout(window, params);
            onDragMoved(x, y);
        } else if (event.getAction() == ACTION_UP) {
            onDragEnded(x, y);
            if (moving) {
                moving = false;
                return true;
            }
        }

        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            DisplayMetrics screenSize = Utils.getScreenSize(windowManager);
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) window.getLayoutParams();
            params.x = params.x > screenSize.heightPixels / 2 ? screenSize.widthPixels : 0;
            params.y = getInitPosition().y;
            windowManager.updateViewLayout(window, params);
        }
    }

    protected Point getInitPosition() {
        return new Point(0, 0);
    }

    protected boolean onBackPressed() {
        return false;
    }

    protected void onDragStarted(float x, float y) {}

    protected void onDragEnded(float x, float y) {}

    protected void onDragMoved(float x, float y) {}

    @Override
    public void onDestroy() {
        windowManager.removeView(window);
        super.onDestroy();
    }
}

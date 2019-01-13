package com.rain.networkproxy.ui;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.support.annotation.NonNull;
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

public abstract class OverlayService extends Service implements View.OnTouchListener {
    private static final long MAX_CLICK_DURATION = 100;

    protected WindowManager windowManager;
    private FrameLayout window;
    private boolean moving = false;
    private long startClickTime = 0;

    protected abstract View onCreateView(@NonNull ViewGroup window);

    protected abstract void onWindowCreate();

    protected abstract void onViewCreated(@NonNull View view);

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        onWindowCreate();
        initWindow();
        onViewCreated(window);
    }

    private void initWindow() {
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
        windowManager.addView(window, initLayoutParams());
    }

    private WindowManager.LayoutParams initLayoutParams() {
        final Point position = getInitPosition();
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
        return params;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
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

            updatePosition((int) (x - view.getWidth() / 2), (int) (y - view.getHeight() * 1.5));
            onDragMoved(x, y);
        } else if (event.getAction() == ACTION_UP) {
            moveViewToEdge();
            onDragEnded(x, y);
            if (moving) {
                moving = false;
                return true;
            }
        }

        return false;
    }

    private void moveViewToEdge() {
        DisplayMetrics screenSize = Utils.getScreenSize(windowManager);
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) window.getLayoutParams();
        updatePosition(
                params.x > screenSize.widthPixels / 2 ? screenSize.widthPixels : 0,
                params.y
        );
    }

    private void updatePosition(int x, int y) {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) window.getLayoutParams();
        params.x = x;
        params.y = y;
        windowManager.updateViewLayout(window, params);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DisplayMetrics screenSize = Utils.getScreenSize(windowManager);
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) window.getLayoutParams();
        updatePosition(
                params.x > screenSize.heightPixels / 2 ? screenSize.widthPixels : 0,
                getInitPosition().y
        );
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

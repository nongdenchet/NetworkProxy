package com.rain.networkproxy.ui.dashboard;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.rain.networkproxy.InstanceProvider;
import com.rain.networkproxy.R;
import com.rain.networkproxy.helper.NPLogger;
import com.rain.networkproxy.model.PendingResponse;
import com.rain.networkproxy.ui.OverlayService;
import com.rain.networkproxy.ui.Utils;
import com.rain.networkproxy.ui.filter.FilterDialog;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static android.view.MotionEvent.ACTION_UP;

public final class Dashboard extends OverlayService implements DashboardAdapter.Listener {
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final DashboardViewModel viewModel = new DashboardViewModel(
            InstanceProvider.instance().provideDispatcher(),
            InstanceProvider.instance().provideStateProvider()
    );

    private DashboardAdapter adapter;
    private DetailDialog detailDialog;
    private FilterDialog filterDialog;
    private Background background;
    private RemoveBar removeBar;
    private TextView tvEmpty;
    private View content;
    private View shortcut;

    @Nullable
    private Disposable hidingDisposable;

    @Override
    public void onProceed(PendingResponse pendingResponse) {
        viewModel.onProceed(pendingResponse);
    }

    @Override
    public void onShow(PendingResponse pendingResponse) {
        detailDialog.show(pendingResponse);
    }

    @Override
    protected View onCreateView(@NonNull ViewGroup window) {
        return LayoutInflater.from(this).inflate(R.layout.network_proxy_dashboard, window, false);
    }

    @Override
    protected void onWindowCreate() {
        adapter = new DashboardAdapter(this, this);
        filterDialog = new FilterDialog(this);
        removeBar = new RemoveBar(this);
        detailDialog = new DetailDialog(this);
        background = new Background(this);
        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideContent();
            }
        });
        background.attach();
    }

    @Override
    protected void onViewCreated(@NonNull View view) {
        initShortcut(view);
        initContent(view);
        initPending(view);
        bindViewModels();
    }

    private void initContent(View view) {
        tvEmpty = view.findViewById(R.id.tvEmpty);
        content = view.findViewById(R.id.content);
        view.findViewById(R.id.ivFilter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDialog.show();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initShortcut(View view) {
        shortcut = view.findViewById(R.id.shortcut);
        shortcut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showContent();
            }
        });
        shortcut.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                final boolean isInRemoveBar = event.getRawY() > removeBar.getY() - removeBar.getHeight();
                if (event.getAction() == ACTION_UP && isInRemoveBar) {
                    stopSelf();
                    return true;
                }
                return Dashboard.this.onTouch(view, event);
            }
        });
    }

    private void showContent() {
        focusWindow();
        focusShortcut();
        background.activateBackground();
        shortcut.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);
    }

    private void hideContent() {
        shortcut.setVisibility(View.VISIBLE);
        content.setVisibility(View.GONE);
        background.deactivateBackground();
        unFocusWindow();
        blurShortcut();
    }

    private void focusShortcut() {
        cancelHiding();
        shortcut.setAlpha(1f);
    }

    private void cancelHiding() {
        if (hidingDisposable != null) {
            hidingDisposable.dispose();
            hidingDisposable = null;
        }
    }

    private void blurShortcut() {
        cancelHiding();
        hidingDisposable = Observable.just(0)
                .delay(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        shortcut.setAlpha(0.25f);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        NPLogger.logError("Dashboard#blurShortcut", throwable);
                    }
                });
    }

    @Override
    protected boolean onBackPressed() {
        hideContent();
        return true;
    }

    private void initPending(View view) {
        ListView lvPending = view.findViewById(R.id.lvPending);
        lvPending.setAdapter(adapter);
    }

    @Override
    protected Point getInitPosition() {
        final DisplayMetrics screenSize = Utils.getScreenSize(windowManager);
        return new Point(screenSize.widthPixels, screenSize.heightPixels / 4 - getResources()
                .getDimensionPixelSize(R.dimen.network_proxy_shortcut_size) / 2);
    }

    private void bindViewModels() {
        disposables.add(viewModel.observePendingResponses()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<PendingResponse>>() {
                    @Override
                    public void accept(List<PendingResponse> pendingResponses) {
                        adapter.submitList(pendingResponses);
                        tvEmpty.setVisibility(pendingResponses.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        NPLogger.logError("observePendingResponses", throwable);
                    }
                }));
    }

    @Override
    protected void onDragStarted(float x, float y) {
        removeBar.attach();
        focusShortcut();
    }

    @Override
    protected void onDragEnded(float x, float y) {
        removeBar.detach();
        blurShortcut();
    }

    @Override
    protected void onDragMoved(float x, float y) {
        removeBar.update(y);
    }

    @Override
    public void onDestroy() {
        detailDialog.hide();
        removeBar.detach();
        background.detach();
        disposables.dispose();
        super.onDestroy();
    }
}

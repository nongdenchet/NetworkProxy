package com.rain.networkproxy.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.rain.networkproxy.InstanceProvider;
import com.rain.networkproxy.R;
import com.rain.networkproxy.helper.NPLogger;
import com.rain.networkproxy.model.PendingResponse;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public final class NPActivity extends AppCompatActivity implements NPAdapter.ItemListener {
    private static final String TAG = "NPActivity";
    private final NPAdapter adapter = new NPAdapter();
    private final CompositeDisposable disposables = new CompositeDisposable();

    private NPViewModel viewModel;

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, NPActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initDependency();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network_proxy_activity_np);
        initViews();
        bindViewModels();
    }

    @Override
    public void onProceed(PendingResponse pendingResponse) {
        viewModel.onProceed(pendingResponse);
        finish();
    }

    private void bindViewModels() {
        disposables.add(viewModel.observePendingResponses()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<PendingResponse>>() {
                    @Override
                    public void accept(List<PendingResponse> pendingResponses) {
                        adapter.submitList(pendingResponses);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        NPLogger.logError(TAG, throwable);
                    }
                }));
    }

    private void initViews() {
        RecyclerView rvPending = findViewById(R.id.rvPending);
        rvPending.setLayoutManager(new LinearLayoutManager(this));
        rvPending.setAdapter(adapter);
        adapter.setItemListener(this);
    }

    private void initDependency() {
        final InstanceProvider instanceProvider = InstanceProvider.instance();
        viewModel = new NPViewModel(
                instanceProvider.provideDispatcher(),
                instanceProvider.provideStateProvider()
        );
    }

    @Override
    protected void onDestroy() {
        disposables.dispose();
        super.onDestroy();
    }
}

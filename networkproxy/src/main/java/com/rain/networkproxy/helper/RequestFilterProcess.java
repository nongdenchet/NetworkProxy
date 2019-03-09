package com.rain.networkproxy.helper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rain.networkproxy.NPCommand;
import com.rain.networkproxy.internal.Dispatcher;
import com.rain.networkproxy.model.FilterItem;
import com.rain.networkproxy.storage.FilterStorage;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;

public final class RequestFilterProcess {
    private final FilterStorage filterStorage;
    private final Dispatcher<NPCommand> dispatcher;

    @Nullable
    private Disposable disposable;

    public RequestFilterProcess(@NonNull FilterStorage filterStorage,
                                @NonNull Dispatcher<NPCommand> dispatcher) {
        this.filterStorage = filterStorage;
        this.dispatcher = dispatcher;
    }

    public void execute() {
        disposeIfNeeded();
        disposable = filterStorage.getItems()
                .switchMapSingle(this::filterItemsToRules)
                .map(NPCommand.ApplyFilter::new)
                .subscribe(dispatcher::dispatch, throwable ->
                        NPLogger.logError("RequestFilterProcess#execute", throwable));
    }

    private Single<List<String>> filterItemsToRules(List<FilterItem> filterItems) {
        return Observable.fromIterable(filterItems)
                .filter(FilterItem::isActive)
                .map(FilterItem::getRule)
                .toList();
    }

    private void disposeIfNeeded() {
        RxUtils.dispose(disposable);
    }
}

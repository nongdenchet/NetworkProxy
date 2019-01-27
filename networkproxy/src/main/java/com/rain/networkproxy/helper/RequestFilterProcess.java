package com.rain.networkproxy.helper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.rain.networkproxy.NPCommand;
import com.rain.networkproxy.internal.Dispatcher;
import com.rain.networkproxy.storage.FilterItem;
import com.rain.networkproxy.storage.FilterStorage;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

import java.util.List;

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
                .switchMapSingle(new Function<List<FilterItem>, SingleSource<List<String>>>() {
                    @Override
                    public SingleSource<List<String>> apply(List<FilterItem> filterItems) {
                        return filterItemsToRules(filterItems);
                    }
                })
                .map(new Function<List<String>, NPCommand.ApplyFilter>() {
                    @Override
                    public NPCommand.ApplyFilter apply(List<String> rules) {
                        return new NPCommand.ApplyFilter(rules);
                    }
                })
                .subscribe(new Consumer<NPCommand.ApplyFilter>() {
                    @Override
                    public void accept(NPCommand.ApplyFilter command) {
                        dispatcher.dispatch(command);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        NPLogger.logError("RequestFilterProcess#execute", throwable);
                    }
                });
    }

    private Single<List<String>> filterItemsToRules(List<FilterItem> filterItems) {
        return Observable.fromIterable(filterItems)
                .filter(new Predicate<FilterItem>() {
                    @Override
                    public boolean test(FilterItem filterItem) {
                        return filterItem.isActive();
                    }
                })
                .map(new Function<FilterItem, String>() {
                    @Override
                    public String apply(FilterItem filterItem) {
                        return filterItem.getRule();
                    }
                })
                .toList();
    }

    private void disposeIfNeeded() {
        RxUtils.dispose(disposable);
    }
}

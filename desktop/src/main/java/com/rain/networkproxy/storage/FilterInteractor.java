package com.rain.networkproxy.storage;

import android.support.annotation.Nullable;

import com.rain.networkproxy.model.FilterItem;
import com.rain.networkproxy.support.Pair;
import com.rain.networkproxy.utils.RxUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public final class FilterInteractor {
    private final FilterStorage filterStorage;
    private final PublishSubject<FilterEvent> events;

    @Nullable
    private Disposable disposable;

    public FilterInteractor(FilterStorage filterStorage) {
        this.filterStorage = filterStorage;
        this.events = PublishSubject.create();
    }

    public void handle(FilterEvent event) {
        events.onNext(event);
    }

    public void start() {
        RxUtils.dispose(disposable);
        disposable = events.serialize()
                .withLatestFrom(filterStorage.getFilters(), Pair::new)
                .map(pair -> reduce(pair.second, pair.first))
                .subscribe(filterStorage::setFilters);
    }

    private List<FilterItem> reduce(List<FilterItem> prev, FilterEvent event) {
        if (event instanceof FilterEvent.Create) {
            return add(prev, (FilterEvent.Create) event);
        }

        if (event instanceof FilterEvent.Update) {
            return update(prev, (FilterEvent.Update) event);
        }

        if (event instanceof FilterEvent.Delete) {
            return delete(prev, (FilterEvent.Delete) event);
        }

        return prev;
    }

    private List<FilterItem> add(List<FilterItem> prev, FilterEvent.Create event) {
        for (FilterItem item : prev) {
            if (item.getRule().equals(event.rule)) {
                return prev;
            }
        }

        final List<FilterItem> result = new ArrayList<>(prev.size());
        result.addAll(prev);
        result.add(new FilterItem(event.rule, true));
        return result;
    }

    private List<FilterItem> delete(List<FilterItem> prev, FilterEvent.Delete event) {
        final List<FilterItem> result = new ArrayList<>(prev.size());
        for (FilterItem item : prev) {
            if (!item.getRule().equals(event.rule)) {
                result.add(item);
            }
        }
        return result;
    }

    private List<FilterItem> update(List<FilterItem> prev, FilterEvent.Update event) {
        final List<FilterItem> result = new ArrayList<>(prev.size());
        for (FilterItem item : prev) {
            if (item.getRule().equals(event.rule)) {
                result.add(new FilterItem(event.rule, event.active));
            } else {
                result.add(item);
            }
        }
        return result;
    }
}

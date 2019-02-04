package com.rain.networkproxy.filter;

import android.support.annotation.Nullable;

import com.rain.networkproxy.model.FilterItem;
import com.rain.networkproxy.support.Pair;
import com.rain.networkproxy.utils.RxUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public final class FilterInteractor {
    private final com.rain.networkproxy.filter.FilterStorage filterStorage;
    private final PublishSubject<com.rain.networkproxy.filter.FilterEvent> events;

    @Nullable
    private Disposable disposable;

    public FilterInteractor(FilterStorage filterStorage) {
        this.filterStorage = filterStorage;
        this.events = PublishSubject.create();
    }

    public void handle(com.rain.networkproxy.filter.FilterEvent event) {
        events.onNext(event);
    }

    public void start() {
        RxUtils.dispose(disposable);
        disposable = events.serialize()
                .withLatestFrom(filterStorage.getFilters(), Pair::new)
                .map(pair -> reduce(pair.second, pair.first))
                .subscribe(filterStorage::setFilters);
    }

    private List<FilterItem> reduce(List<FilterItem> prev, com.rain.networkproxy.filter.FilterEvent event) {
        if (event instanceof com.rain.networkproxy.filter.FilterEvent.Create) {
            return add(prev, (com.rain.networkproxy.filter.FilterEvent.Create) event);
        }

        if (event instanceof com.rain.networkproxy.filter.FilterEvent.Update) {
            return update(prev, (com.rain.networkproxy.filter.FilterEvent.Update) event);
        }

        if (event instanceof com.rain.networkproxy.filter.FilterEvent.Delete) {
            return delete(prev, (com.rain.networkproxy.filter.FilterEvent.Delete) event);
        }

        return prev;
    }

    private List<FilterItem> add(List<FilterItem> prev, com.rain.networkproxy.filter.FilterEvent.Create event) {
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

    private List<FilterItem> delete(List<FilterItem> prev, com.rain.networkproxy.filter.FilterEvent.Delete event) {
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

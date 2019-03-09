package com.rain.networkproxy.ui.filter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rain.networkproxy.R;
import com.rain.networkproxy.helper.NPLogger;
import com.rain.networkproxy.helper.ResourceProvider;
import com.rain.networkproxy.helper.RxUtils;
import com.rain.networkproxy.model.FilterItem;
import com.rain.networkproxy.storage.FilterStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

final class FilterDialogViewModel {
    private static final int SELECT_ALL_POSITION = 0;

    private final PublishSubject<FilterAction> actions = PublishSubject.create();
    private final FilterStorage filterStorage;
    private final ResourceProvider resourceProvider;

    @Nullable
    private Disposable disposable;

    FilterDialogViewModel(@NonNull FilterStorage filterStorage,
                          @NonNull ResourceProvider resourceProvider) {
        this.filterStorage = filterStorage;
        this.resourceProvider = resourceProvider;
    }

    void initialize() {
        disposable = actions.serialize()
                .withLatestFrom(filterStorage.getItems(), this::reduce)
                .map(Collections::unmodifiableList)
                .subscribe(filterStorage::storeItems, throwable ->
                        NPLogger.logError("FilterDialogViewModel#actions", throwable));
    }

    private List<FilterItem> reduce(FilterAction action, List<FilterItem> prev) {
        if (action instanceof FilterAction.Remove) {
            return remove(prev, ((FilterAction.Remove) action).position);
        } else if (action instanceof FilterAction.Update) {
            final FilterAction.Update _action = (FilterAction.Update) action;
            return update(prev, _action.position, _action.active);
        } else if (action instanceof FilterAction.Add) {
            return add(prev, ((FilterAction.Add) action).rule);
        }

        return prev;
    }

    private List<FilterItem> add(List<FilterItem> prev, String rule) {
        final List<FilterItem> items = new ArrayList<>(prev.size() + 1);
        items.addAll(prev);
        items.add(0, new FilterItem(rule, true));
        return items;
    }

    private List<FilterItem> remove(List<FilterItem> prev, final int position) {
        final List<FilterItem> items = new ArrayList<>(prev.size());
        for (int i = 0; i < prev.size(); i++) {
            if (i != position - 1) {
                items.add(prev.get(i));
            }
        }
        return items;
    }

    private List<FilterItem> update(List<FilterItem> prev, final int position, boolean active) {
        if (position == SELECT_ALL_POSITION) {
            return selectAll(prev, active);
        }

        final List<FilterItem> items = new ArrayList<>(prev.size());
        for (int i = 0; i < prev.size(); i++) {
            if (i == position - 1) {
                items.add(new FilterItem(prev.get(i).getRule(), active));
            } else {
                items.add(prev.get(i));
            }
        }
        return items;
    }

    private List<FilterItem> selectAll(List<FilterItem> prev, final boolean active) {
        final List<FilterItem> items = new ArrayList<>(prev.size());
        for (FilterItem item : prev) {
            items.add(new FilterItem(item.getRule(), active));
        }
        return items;
    }

    void post(FilterAction action) {
        actions.onNext(action);
    }

    void dispose() {
        RxUtils.dispose(disposable);
    }

    Observable<List<FilterItemViewModel>> observeItems() {
        return filterStorage.getItems()
                .map(filterItems -> {
                    if (filterItems.isEmpty()) {
                        return Collections.emptyList();
                    }

                    boolean allSelected = true;
                    final List<FilterItemViewModel> items = new ArrayList<>(filterItems.size() + 1);

                    for (FilterItem filterItem : filterItems) {
                        allSelected = allSelected && filterItem.isActive();
                        items.add(new FilterItemViewModel(
                                filterItem.getRule(),
                                filterItem.isActive()
                        ));
                    }
                    items.add(SELECT_ALL_POSITION, new FilterItemViewModel(
                            resourceProvider.getString(R.string.network_proxy_select_all),
                            allSelected
                    ));

                    return items;
                });
    }
}

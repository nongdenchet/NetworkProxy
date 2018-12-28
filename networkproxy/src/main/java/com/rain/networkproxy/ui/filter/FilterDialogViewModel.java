package com.rain.networkproxy.ui.filter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.rain.networkproxy.R;
import com.rain.networkproxy.helper.NPLogger;
import com.rain.networkproxy.helper.ResourceProvider;
import com.rain.networkproxy.storage.FilterItem;
import com.rain.networkproxy.storage.FilterStorage;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.List;

final class FilterDialogViewModel {
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
                .withLatestFrom(filterStorage.getItems(), new BiFunction<FilterAction, List<FilterItem>, List<FilterItem>>() {
                    @Override
                    public List<FilterItem> apply(FilterAction action, List<FilterItem> filterItems) {
                        return reduce(filterItems, action);
                    }
                })
                .subscribe(new Consumer<List<FilterItem>>() {
                    @Override
                    public void accept(List<FilterItem> filterItems) {
                        filterStorage.storeItems(filterItems);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        NPLogger.logError("FilterDialogViewModel#actions", throwable);
                    }
                });
    }

    private List<FilterItem> reduce(List<FilterItem> prev, FilterAction action) {
        if (action instanceof FilterAction.SelectAll) {
            return selectAll(prev, ((FilterAction.SelectAll) action).select);
        } else if (action instanceof FilterAction.Remove) {
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
            if (i != position) {
                items.add(prev.get(i));
            }
        }
        return items;
    }

    private List<FilterItem> update(List<FilterItem> prev, final int position, boolean active) {
        final List<FilterItem> items = new ArrayList<>(prev.size());
        for (int i = 0; i < prev.size(); i++) {
            if (i == position) {
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
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
    }

    Observable<List<FilterItemViewModel>> observeItems() {
        return filterStorage.getItems()
                .map(new Function<List<FilterItem>, List<FilterItemViewModel>>() {
                    @Override
                    public List<FilterItemViewModel> apply(List<FilterItem> filterItems) {
                        boolean allSelected = true;
                        final List<FilterItemViewModel> items = new ArrayList<>(filterItems.size() + 1);

                        for (FilterItem filterItem : filterItems) {
                            allSelected = allSelected && filterItem.isActive();
                            items.add(new FilterItemViewModel(
                                    FilterItemViewModel.Type.ITEM,
                                    filterItem.getRule(),
                                    filterItem.isActive()
                            ));
                        }
                        items.add(0, new FilterItemViewModel(
                                FilterItemViewModel.Type.SELECT_ALL,
                                resourceProvider.getString(R.string.network_proxy_select_all),
                                allSelected
                        ));

                        return items;
                    }
                });
    }
}

package com.rain.networkproxy.storage;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public final class FakeFilterStorage implements FilterStorage {
    private final BehaviorSubject<List<FilterItem>> items = BehaviorSubject.createDefault(Collections.<FilterItem>emptyList());

    @Override
    public Observable<List<FilterItem>> getItems() {
        return items.hide();
    }

    @Override
    public void storeItems(@NonNull List<FilterItem> items) {
        this.items.onNext(items);
    }
}

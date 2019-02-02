package com.rain.networkproxy.storage;

import android.support.annotation.NonNull;

import com.rain.networkproxy.model.FilterItem;

import io.reactivex.Observable;

import java.util.List;

public interface FilterStorage {
    Observable<List<FilterItem>> getItems();

    void storeItems(@NonNull List<FilterItem> items);
}

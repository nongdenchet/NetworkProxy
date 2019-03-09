package com.rain.networkproxy.storage;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rain.networkproxy.model.FilterItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public final class FilterStorageImpl implements FilterStorage {
    private static final String KEY = "filter_items";
    private static final Type TYPE = new TypeToken<List<FilterItem>>() {}.getType();

    private final Gson gson = new Gson();
    private final SharedPreferences sharedPreferences;
    private final Observable<String> keyChanges;

    public FilterStorageImpl(@NonNull SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        this.keyChanges = Utils.observeKeys(sharedPreferences)
                .share();
    }

    @Override
    public Observable<List<FilterItem>> getItems() {
        return keyChanges.startWith(KEY)
                .filter(KEY::equals)
                .map(key -> sharedPreferences.getString(key, ""))
                .map((Function<String, List<FilterItem>>) value -> {
                    if (value == null || value.isEmpty()) {
                        return new ArrayList<>();
                    }

                    return gson.fromJson(value, TYPE);
                })
                .map(Collections::unmodifiableList)
                .distinctUntilChanged();
    }

    @Override
    public void storeItems(@NonNull List<FilterItem> items) {
        sharedPreferences.edit()
                .putString(KEY, gson.toJson(items, TYPE))
                .apply();
    }
}

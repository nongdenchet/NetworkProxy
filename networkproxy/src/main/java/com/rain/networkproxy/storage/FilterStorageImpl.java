package com.rain.networkproxy.storage;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

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
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(String key) {
                        return KEY.equals(key);
                    }
                })
                .map(new Function<String, String>() {
                    @Override
                    public String apply(String key) {
                        return sharedPreferences.getString(key, "");
                    }
                })
                .map(new Function<String, List<FilterItem>>() {
                    @Override
                    public List<FilterItem> apply(String value) {
                        if (value == null || value.isEmpty()) {
                            return new ArrayList<>();
                        }

                        return gson.fromJson(value, TYPE);
                    }
                })
                .map(new Function<List<FilterItem>, List<FilterItem>>() {
                    @Override
                    public List<FilterItem> apply(List<FilterItem> filterItems) {
                        return Collections.unmodifiableList(filterItems);
                    }
                })
                .distinctUntilChanged();
    }

    @Override
    public void storeItems(@NonNull List<FilterItem> items) {
        sharedPreferences.edit()
                .putString(KEY, gson.toJson(items, TYPE))
                .apply();
    }
}

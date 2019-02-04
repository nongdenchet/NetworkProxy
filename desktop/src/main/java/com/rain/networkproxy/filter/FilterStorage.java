package com.rain.networkproxy.filter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rain.networkproxy.model.FilterItem;

import java.util.List;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

public final class FilterStorage {
    private final static String FILTER_STORAGE = "FILTER_STORAGE";

    private final Preferences preferences = Preferences.userRoot().node(this.getClass().getName());
    private final Gson gson = new Gson();

    public FilterStorage() {
        System.out.println(preferences.absolutePath());
    }

    private List<FilterItem> getStoredFilters() {
        return getStoredFilters(preferences.get(FILTER_STORAGE, "[]"));
    }

    private List<FilterItem> getStoredFilters(String raw) {
        return gson.fromJson(raw, new TypeToken<List<FilterItem>>() {
        }.getType());
    }

    public Observable<List<FilterItem>> getFilters() {
        return Observable.create((ObservableOnSubscribe<List<FilterItem>>) emitter -> {
            final PreferenceChangeListener listener = evt -> {
                if (FILTER_STORAGE.equals(evt.getKey())) {
                    emitter.onNext(getStoredFilters(evt.getNewValue()));
                }
            };
            preferences.addPreferenceChangeListener(listener);
            emitter.setCancellable(() -> preferences.removePreferenceChangeListener(listener));
        }).startWith(getStoredFilters());
    }

    void setFilters(List<FilterItem> filters) {
        preferences.put(FILTER_STORAGE, gson.toJson(filters));
    }
}

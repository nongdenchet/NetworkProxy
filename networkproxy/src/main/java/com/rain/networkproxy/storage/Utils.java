package com.rain.networkproxy.storage;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import io.reactivex.Observable;

final class Utils {

    private Utils() {}

    static Observable<String> observeKeys(@NonNull final SharedPreferences sharedPreferences) {
        return Observable.create(emitter -> {
            final SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences1, key) -> emitter.onNext(key);
            emitter.setCancellable(() -> sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener));
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
        });
    }
}

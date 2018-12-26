package com.rain.networkproxy.storage;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Cancellable;

final class Utils {

    private Utils() {}

    static Observable<String> observeKeys(@NonNull final SharedPreferences sharedPreferences) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> emitter) {
                final SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                        emitter.onNext(key);
                    }
                };
                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() {
                        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
                    }
                });
                sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
            }
        });
    }
}

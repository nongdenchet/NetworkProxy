package com.rain.networkproxy.internal;

import io.reactivex.Observable;

public interface StateProvider<T> {
    Observable<T> state();

    T currentState();
}

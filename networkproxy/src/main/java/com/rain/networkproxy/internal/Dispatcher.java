package com.rain.networkproxy.internal;

public interface Dispatcher<T> {
    void dispatch(T command);
}

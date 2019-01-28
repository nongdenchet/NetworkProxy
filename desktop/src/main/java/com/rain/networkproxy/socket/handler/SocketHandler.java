package com.rain.networkproxy.socket.handler;

import android.support.annotation.Nullable;

public interface SocketHandler<T> {
    void execute(@Nullable T payload);
}

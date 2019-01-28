package com.rain.networkproxy.socket.handler;

import android.support.annotation.Nullable;
import com.rain.networkproxy.DesktopState;
import com.rain.networkproxy.model.RequestFilter;

public class FilterHandler implements SocketHandler<RequestFilter> {
    private final DesktopState desktopState;

    public FilterHandler(DesktopState desktopState) {
        this.desktopState = desktopState;
    }

    @Override
    public void execute(@Nullable RequestFilter payload) {
        if (payload != null) {
            desktopState.setFilter(payload);
        }
    }
}

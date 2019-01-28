package com.rain.networkproxy.socket.handler;

import android.support.annotation.Nullable;
import com.rain.networkproxy.DesktopState;
import com.rain.networkproxy.model.InternalResponse;

import java.util.List;

public class InternalResponseHandler implements SocketHandler<List<InternalResponse>> {
    private final DesktopState desktopState;

    public InternalResponseHandler(DesktopState desktopState) {
        this.desktopState = desktopState;
    }

    @Override
    public void execute(@Nullable List<InternalResponse> payload) {
        desktopState.setResponses(payload);
    }
}

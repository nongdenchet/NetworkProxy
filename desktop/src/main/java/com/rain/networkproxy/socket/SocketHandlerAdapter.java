package com.rain.networkproxy.socket;

import com.rain.networkproxy.DesktopState;
import com.rain.networkproxy.socket.handler.FilterHandler;
import com.rain.networkproxy.socket.handler.InternalResponseHandler;
import com.rain.networkproxy.socket.handler.SocketHandler;

import java.util.HashMap;

import static com.rain.networkproxy.model.SocketMessage.FILTER;
import static com.rain.networkproxy.model.SocketMessage.INTERNAL_RESPONSES;

final class SocketHandlerAdapter {
    private final HashMap<Integer, SocketHandler> adapter = new HashMap<>();

    SocketHandlerAdapter(DesktopState desktopState) {
        this.adapter.put(INTERNAL_RESPONSES, new InternalResponseHandler(desktopState));
        this.adapter.put(FILTER, new FilterHandler(desktopState));
    }

    SocketHandler getSocketHandler(int type) {
        return adapter.get(type);
    }
}

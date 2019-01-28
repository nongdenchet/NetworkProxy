package com.rain.networkproxy.socket;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rain.networkproxy.model.InternalResponse;
import com.rain.networkproxy.model.RequestFilter;
import com.rain.networkproxy.model.SocketMessage;
import org.json.JSONObject;

import java.util.List;

import static com.rain.networkproxy.model.SocketMessage.FILTER;
import static com.rain.networkproxy.model.SocketMessage.INTERNAL_RESPONSES;

final class SocketMessageParser {
    private final Gson gson = new Gson();

    SocketMessage parseMessage(String message) {
        final JSONObject jsonObject = new JSONObject(message);
        final int type = jsonObject.getInt("type");
        final String payload = jsonObject.get("payload").toString();

        if (type == INTERNAL_RESPONSES) {
            return new SocketMessage<>(type, gson.fromJson(payload, new TypeToken<List<InternalResponse>>() {
            }.getType()));
        } else if (type == FILTER) {
            return new SocketMessage<>(type, gson.fromJson(payload, RequestFilter.class));
        } else {
            throw new IllegalStateException("type not support");
        }
    }
}

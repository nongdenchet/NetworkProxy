package com.rain.networkproxy.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public final class SocketMessageParser {
    private static final Type INTERNAL_TYPE = new TypeToken<List<InternalResponse>>() {}.getType();
    private static final Type FILTER_TYPE = new TypeToken<List<String>>() {}.getType();

    private final Gson gson;

    public SocketMessageParser(Gson gson) {
        this.gson = gson;
    }

    public SocketMessage parseMessage(String message) {
        final JsonParser parser = new JsonParser();
        final JsonObject jsonObject = parser.parse(message).getAsJsonObject();
        final int type = jsonObject.getAsJsonPrimitive("type").getAsInt();
        final String payload = jsonObject.get("payload").toString();

        if (type == SocketMessage.INTERNAL_RESPONSES) {
            return new SocketMessage<>(type, gson.fromJson(payload, INTERNAL_TYPE));
        } else if (type == SocketMessage.FILTER) {
            return new SocketMessage<>(type, gson.fromJson(payload, FILTER_TYPE));
        } else if (type == SocketMessage.INSTRUCTION) {
            return new SocketMessage<>(type, gson.fromJson(payload, Instruction.class));
        } else {
            throw new IllegalStateException("type not support");
        }
    }
}

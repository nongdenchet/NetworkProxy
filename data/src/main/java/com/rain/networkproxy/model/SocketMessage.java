package com.rain.networkproxy.model;

import android.support.annotation.Nullable;
import com.google.gson.annotations.SerializedName;

public final class SocketMessage<T> {
    public static int INTERNAL_RESPONSES = 1;
    public static int FILTER = 2;
    public static int INSTRUCTION = 3;

    @SerializedName("type")
    private final int type;

    @Nullable
    @SerializedName("payload")
    private final T payload;

    public SocketMessage(int type, @Nullable T payload) {
        this.type = type;
        this.payload = payload;
    }

    public int getType() {
        return type;
    }

    @Nullable
    public T getPayload() {
        return payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SocketMessage<?> that = (SocketMessage<?>) o;

        if (type != that.type) {
            return false;
        }
        return payload != null ? payload.equals(that.payload) : that.payload == null;
    }

    @Override
    public int hashCode() {
        int result = type;
        result = 31 * result + (payload != null ? payload.hashCode() : 0);
        return result;
    }
}

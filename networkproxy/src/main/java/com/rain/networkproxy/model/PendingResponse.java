package com.rain.networkproxy.model;

import android.support.annotation.NonNull;

import okhttp3.Response;

public final class PendingResponse {
    private final String id;
    private final Response response;

    public PendingResponse(@NonNull String id, @NonNull Response response) {
        this.id = id;
        this.response = response;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public Response getResponse() {
        return response;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PendingResponse that = (PendingResponse) o;
        return id.equals(that.id) && response.equals(that.response);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + response.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PendingResponse{"
                + "id='" + id + '\''
                + ", response=" + response
                + '}';
    }
}

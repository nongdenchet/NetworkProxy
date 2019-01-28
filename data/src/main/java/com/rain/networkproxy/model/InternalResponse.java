package com.rain.networkproxy.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.annotations.SerializedName;

public final class InternalResponse {
    @SerializedName("id")
    private final String id;

    @SerializedName("url")
    private final String url;

    @SerializedName("body")
    private final String body;

    @SerializedName("status")
    private final int status;

    public InternalResponse(@NonNull String id, @NonNull String url, @Nullable String body, int status) {
        this.id = id;
        this.url = url;
        this.body = body;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public int getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InternalResponse that = (InternalResponse) o;

        if (status != that.status) {
            return false;
        }
        if (!url.equals(that.url)) {
            return false;
        }
        if (!id.equals(that.id)) {
            return false;
        }
        return body != null ? body.equals(that.body) : that.body == null;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + status;
        result = 31 * result + url.hashCode();
        return result;
    }
}

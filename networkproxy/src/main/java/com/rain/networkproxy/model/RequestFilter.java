package com.rain.networkproxy.model;

import android.support.annotation.NonNull;

import java.util.List;

public final class RequestFilter {
    private final List<String> rules;

    public RequestFilter(@NonNull List<String> rules) {
        this.rules = rules;
    }

    public boolean isMatch(@NonNull String url) {
        return rules.contains(url);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RequestFilter requestFilter = (RequestFilter) o;
        return rules.equals(requestFilter.rules);
    }

    @Override
    public int hashCode() {
        return rules.hashCode();
    }

    @Override
    public String toString() {
        return "RequestFilter{"
                + "rules=" + rules
                + '}';
    }
}

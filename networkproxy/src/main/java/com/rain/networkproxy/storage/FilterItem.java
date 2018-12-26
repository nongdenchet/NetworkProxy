package com.rain.networkproxy.storage;

import android.support.annotation.NonNull;
import com.google.gson.annotations.SerializedName;

public final class FilterItem {

    @SerializedName("rule")
    private final String rule;

    @SerializedName("active")
    private final boolean active;

    public FilterItem(@NonNull String rule, boolean active) {
        this.rule = rule;
        this.active = active;
    }

    public String getRule() {
        return rule;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FilterItem that = (FilterItem) o;

        return active == that.active && rule.equals(that.rule);
    }

    @Override
    public int hashCode() {
        int result = rule.hashCode();
        result = 31 * result + (active ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FilterItem{"
                + "rule='" + rule
                + ", active=" + active +
                '}';
    }
}

package com.rain.networkproxy.ui.filter;

import android.support.annotation.NonNull;

final class FilterItemViewModel {
    private final String name;
    private final boolean active;

    FilterItemViewModel(@NonNull String name, boolean active) {
        this.name = name;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    boolean isActive() {
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

        FilterItemViewModel that = (FilterItemViewModel) o;

        return name.equals(that.name) && active == that.active;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (active ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FilterItemViewModel{"
                + ", name='" + name
                + ", active=" + active +
                '}';
    }
}

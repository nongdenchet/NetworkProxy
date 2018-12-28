package com.rain.networkproxy.ui.filter;

import android.support.annotation.NonNull;

final class FilterItemViewModel {
    private final Type type;
    private final String name;
    private final boolean active;

    enum Type {
        ITEM, SELECT_ALL
    }

    FilterItemViewModel(@NonNull Type type, @NonNull String name, boolean active) {
        this.type = type;
        this.name = name;
        this.active = active;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
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

        FilterItemViewModel that = (FilterItemViewModel) o;

        return name.equals(that.name)
                && active == that.active
                && type == that.type;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (active ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FilterItemViewModel{"
                + "type=" + type
                + ", name='" + name
                + ", active=" + active +
                '}';
    }
}

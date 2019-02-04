package com.rain.networkproxy.support;

public final class Pair<A, B> {
    public final A first;
    public final B second;

    public Pair(A first, B second) {
        if (first == null) {
            throw new IllegalArgumentException("first is null");
        }
        if (second == null) {
            throw new IllegalArgumentException("second is null");
        }
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Pair<?, ?> pair = (Pair<?, ?>) o;

        return first.equals(pair.first) && second.equals(pair.second);
    }

    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = 31 * result + second.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}

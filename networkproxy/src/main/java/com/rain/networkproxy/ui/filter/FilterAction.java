package com.rain.networkproxy.ui.filter;

abstract class FilterAction {
    static final class Add extends FilterAction {
        final String rule;

        Add(String rule) {
            this.rule = rule;
        }
    }

    static final class Remove extends FilterAction {
        final int position;

        Remove(int position) {
            this.position = position;
        }
    }

    static final class Update extends FilterAction {
        final int position;
        final boolean active;

        Update(int position, boolean active) {
            this.position = position;
            this.active = active;
        }
    }
}

package com.rain.networkproxy.storage;

public abstract class FilterEvent {
    public static class Update extends FilterEvent {
        public final String rule;
        public final boolean active;

        public Update(String rule, boolean active) {
            this.rule = rule;
            this.active = active;
        }
    }

    public static class Create extends FilterEvent {
        public final String rule;

        public Create(String rule) {
            this.rule = rule;
        }
    }

    public static class Delete extends FilterEvent {
        public final String rule;

        public Delete(String rule) {
            this.rule = rule;
        }
    }
}

package com.rain.networkproxy.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class Instruction {
    private final String id;
    private final Input input;

    public static final Instruction EMPTY = new Instruction("", new Input());

    public Instruction(@NonNull String id, @NonNull Input input) {
        this.id = id;
        this.input = input;
    }

    public boolean isEmpty() {
        return id.equals("");
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public Input getInput() {
        return input;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Instruction that = (Instruction) o;
        return id.equals(that.id)
                && input.equals(that.input);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + input.hashCode();
        return result;
    }

    public static final class Input {
        private final Integer status;
        private final String body;

        public Input() {
            this(null, null);
        }

        public Input(@Nullable Integer status, @Nullable String body) {
            this.status = status;
            this.body = body;
        }

        @Nullable
        public Integer getStatus() {
            return status;
        }

        @Nullable
        public String getBody() {
            return body;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Input input = (Input) o;
            if (status != null ? !status.equals(input.status) : input.status != null) {
                return false;
            }
            return body != null ? body.equals(input.body) : input.body == null;
        }

        @Override
        public int hashCode() {
            int result = status != null ? status.hashCode() : 0;
            result = 31 * result + (body != null ? body.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Input{"
                    + "status=" + status
                    + "body= " + body
                    + '}';
        }
    }

    @Override
    public String toString() {
        return "Instruction{"
                + "id='" + id + '\''
                + ", input=" + input
                + '}';
    }
}

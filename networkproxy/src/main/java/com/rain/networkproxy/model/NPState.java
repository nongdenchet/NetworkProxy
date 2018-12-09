package com.rain.networkproxy.model;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;

public final class NPState {
    private final RequestFilter requestFilter;
    private final List<PendingResponse> responses;
    private final List<Instruction> instructions;

    public static final NPState DEFAULT = new NPState(
            new RequestFilter(Collections.<String>emptyList()),
            Collections.<PendingResponse>emptyList(),
            Collections.<Instruction>emptyList()
    );

    private NPState(@NonNull RequestFilter requestFilter,
                    @NonNull List<PendingResponse> responses,
                    @NonNull List<Instruction> instructions) {
        this.requestFilter = requestFilter;
        this.responses = responses;
        this.instructions = instructions;
    }

    @NonNull
    public RequestFilter getRequestFilter() {
        return requestFilter;
    }

    @NonNull
    public List<PendingResponse> getResponses() {
        return responses;
    }

    @NonNull
    public List<Instruction> getInstructions() {
        return instructions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NPState that = (NPState) o;
        return requestFilter.equals(that.requestFilter)
                && instructions.equals(that.instructions)
                && responses.equals(that.responses);
    }

    @Override
    public int hashCode() {
        int result = requestFilter.hashCode();
        result = 31 * result + responses.hashCode();
        result = 31 * result + instructions.hashCode();
        return result;
    }

    public Builder newBuilder() {
        return new Builder().requestFilter(requestFilter)
                .instructions(instructions)
                .responses(responses);
    }

    public static final class Builder {
        private RequestFilter requestFilter;
        private List<PendingResponse> responses;
        private List<Instruction> instructions;

        private Builder() {
        }

        public Builder requestFilter(@NonNull RequestFilter requestFilter) {
            this.requestFilter = requestFilter;
            return this;
        }

        public Builder responses(@NonNull List<PendingResponse> responses) {
            this.responses = responses;
            return this;
        }

        public Builder instructions(@NonNull List<Instruction> instructions) {
            this.instructions = instructions;
            return this;
        }

        public NPState build() {
            return new NPState(requestFilter, responses, instructions);
        }
    }

    @Override
    public String toString() {
        return "NPState{"
                + "requestFilter=" + requestFilter
                + ", responses=" + responses
                + ", instructions=" + instructions
                + '}';
    }
}

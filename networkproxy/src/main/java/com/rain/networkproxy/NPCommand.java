package com.rain.networkproxy;

import android.support.annotation.NonNull;

import com.rain.networkproxy.model.Instruction;
import com.rain.networkproxy.model.PendingResponse;

import java.util.List;

public abstract class NPCommand {
    public static class ApplyFilter extends NPCommand {
        private final List<String> rules;

        public ApplyFilter(@NonNull List<String> rules) {
            this.rules = rules;
        }

        List<String> getRules() {
            return rules;
        }
    }

    public static class ApplyInstructions extends NPCommand {
        private final List<Instruction> instructions;

        public ApplyInstructions(@NonNull List<Instruction> instructions) {
            this.instructions = instructions;
        }

        List<Instruction> getInstructions() {
            return instructions;
        }
    }

    public static class AddPendingResponse extends NPCommand {
        private final PendingResponse pendingResponse;

        public AddPendingResponse(@NonNull PendingResponse pendingResponse) {
            this.pendingResponse = pendingResponse;
        }

        PendingResponse getPendingResponse() {
            return pendingResponse;
        }
    }
}

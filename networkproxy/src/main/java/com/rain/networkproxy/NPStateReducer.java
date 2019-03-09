package com.rain.networkproxy;

import com.rain.networkproxy.helper.NPLogger;
import com.rain.networkproxy.model.Instruction;
import com.rain.networkproxy.model.NPState;
import com.rain.networkproxy.model.PendingResponse;
import com.rain.networkproxy.model.RequestFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;

final class NPStateReducer implements BiFunction<NPState, NPCommand, NPState> {

    @Override
    public NPState apply(NPState prev, NPCommand command) {
        if (command instanceof NPCommand.ApplyFilter) {
            return applyFilter(prev, ((NPCommand.ApplyFilter) command).getRules());
        } else if (command instanceof NPCommand.AddPendingResponse) {
            return addPendingResponse(prev, ((NPCommand.AddPendingResponse) command).getPendingResponse());
        } else if (command instanceof NPCommand.ApplyInstructions) {
            return applyInstructions(prev, ((NPCommand.ApplyInstructions) command).getInstructions());
        } else if (command instanceof NPCommand.SkipAllPendingResponse) {
            return skipAllPendingResponse(prev);
        }

        return prev;
    }

    private NPState skipAllPendingResponse(NPState prev) {
        NPLogger.log("Skip all pending response");

        final List<Instruction> newInstructions = new ArrayList<>(prev.getResponses().size());
        for (PendingResponse pendingResponse : prev.getResponses()) {
            newInstructions.add(new Instruction(pendingResponse.getId(), new Instruction.Input()));
        }
        
        return prev.newBuilder()
                .responses(Collections.emptyList())
                .instructions(newInstructions)
                .build();
    }

    private NPState applyInstructions(NPState prev, List<Instruction> instructions) {
        NPLogger.log("Applying instructions: " + instructions);

        final Set<String> instructionIds = toInstructionIds(instructions);
        final Set<String> responseIds = toResponseIds(prev.getResponses());

        final List<PendingResponse> newPendingResponse = new ArrayList<>();
        for (PendingResponse response : prev.getResponses()) {
            if (!instructionIds.contains(response.getId())) {
                newPendingResponse.add(response);
            }
        }

        final int listSize = instructions.size() + prev.getInstructions().size();
        final List<Instruction> newInstructions = new ArrayList<>(listSize);
        for (Instruction instruction : prev.getInstructions()) {
            if (!instructionIds.contains(instruction.getId())) {
                newInstructions.add(instruction);
            }
        }
        for (Instruction instruction : instructions) {
            if (responseIds.contains(instruction.getId())) {
                newInstructions.add(instruction);
            }
        }

        return prev.newBuilder()
                .responses(newPendingResponse)
                .instructions(newInstructions)
                .build();
    }

    private Set<String> toInstructionIds(List<Instruction> instructions) {
        final List<String> ids = Observable.fromIterable(instructions)
                .map(Instruction::getId)
                .toList()
                .blockingGet();

        return new HashSet<>(ids);
    }

    private Set<String> toResponseIds(List<PendingResponse> responses) {
        final List<String> ids = Observable.fromIterable(responses)
                .map(PendingResponse::getId)
                .toList()
                .blockingGet();

        return new HashSet<>(ids);
    }

    private NPState addPendingResponse(NPState prev, PendingResponse newResponse) {
        NPLogger.log("Adding response: " + newResponse);

        final List<PendingResponse> responses = new ArrayList<>(prev.getResponses().size());
        boolean contains = false;

        for (PendingResponse response : prev.getResponses()) {
            if (response.getId().equals(newResponse.getId())) {
                contains = true;
            }
            responses.add(response);
        }

        if (!contains) {
            responses.add(newResponse);
        }

        return prev.newBuilder()
                .responses(responses)
                .build();
    }

    private NPState applyFilter(NPState prev, List<String> rules) {
        NPLogger.log("Applying filter: " + rules.toString());

        return prev.newBuilder()
                .requestFilter(new RequestFilter(Collections.unmodifiableList(rules)))
                .build();
    }
}

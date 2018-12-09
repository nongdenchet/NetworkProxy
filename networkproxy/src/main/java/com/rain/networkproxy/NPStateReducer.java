package com.rain.networkproxy;

import android.util.Log;

import com.rain.networkproxy.model.Instruction;
import com.rain.networkproxy.model.NPState;
import com.rain.networkproxy.model.PendingResponse;
import com.rain.networkproxy.model.RequestFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

final class NPStateReducer implements BiFunction<NPState, NPCommand, NPState> {

    @Override
    public NPState apply(NPState prev, NPCommand command) {
        if (command instanceof NPCommand.ApplyFilter) {
            return applyFilter(prev, ((NPCommand.ApplyFilter) command).getRules());
        } else if (command instanceof NPCommand.AddPendingResponse) {
            return addPendingResponse(prev, ((NPCommand.AddPendingResponse) command).getPendingResponse());
        } else if (command instanceof NPCommand.ApplyInstructions) {
            return applyInstructions(prev, ((NPCommand.ApplyInstructions) command).getInstructions());
        }

        return prev;
    }

    private NPState applyInstructions(NPState prev, List<Instruction> instructions) {
        Log.d(Constants.TAG, "Applying instructions: " + instructions);

        final Set<String> ids = toIds(instructions);
        final int listSize = instructions.size() + prev.getInstructions().size();
        final List<Instruction> newInstructions = new ArrayList<>(listSize);

        for (Instruction instruction : prev.getInstructions()) {
            if (!ids.contains(instruction.getId())) {
                newInstructions.add(instruction);
            }
        }
        newInstructions.addAll(instructions);

        return prev.newBuilder()
                .instructions(newInstructions)
                .build();
    }

    private Set<String> toIds(List<Instruction> instructions) {
        final List<String> ids = Observable.fromIterable(instructions)
                .map(new Function<Instruction, String>() {
                    @Override
                    public String apply(Instruction instruction) {
                        return instruction.getId();
                    }
                })
                .toList()
                .blockingGet();

        return new HashSet<>(ids);
    }

    private NPState addPendingResponse(NPState prev, PendingResponse newResponse) {
        Log.d(Constants.TAG, "Adding response: " + newResponse);

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
        Log.d(Constants.TAG, "Applying filter: " + rules.toString());

        return prev.newBuilder()
                .requestFilter(new RequestFilter(rules))
                .build();
    }
}

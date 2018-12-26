package com.rain.networkproxy.ui.dashboard;

import com.rain.networkproxy.NPCommand;
import com.rain.networkproxy.internal.Dispatcher;
import com.rain.networkproxy.internal.StateProvider;
import com.rain.networkproxy.model.Instruction;
import com.rain.networkproxy.model.NPState;
import com.rain.networkproxy.model.PendingResponse;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

final class DashboardViewModel {
    private final Dispatcher<NPCommand> dispatcher;
    private final StateProvider<NPState> stateProvider;

    DashboardViewModel(Dispatcher<NPCommand> dispatcher, StateProvider<NPState> stateProvider) {
        this.dispatcher = dispatcher;
        this.stateProvider = stateProvider;
    }

    void onProceed(PendingResponse pendingResponse) {
        final Instruction instruction = new Instruction(pendingResponse.getId(), new Instruction.Input());
        final List<Instruction> instructions = Collections.singletonList(instruction);
        final NPCommand command = new NPCommand.ApplyInstructions(instructions);

        dispatcher.dispatch(command);
    }

    Observable<List<PendingResponse>> observePendingResponses() {
        return stateProvider.state()
                .map(new Function<NPState, List<PendingResponse>>() {
                    @Override
                    public List<PendingResponse> apply(NPState state) {
                        return state.getResponses();
                    }
                })
                .distinctUntilChanged();
    }
}

package com.rain.networkproxy.helper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.rain.networkproxy.NPCommand;
import com.rain.networkproxy.internal.Dispatcher;
import com.rain.networkproxy.model.Instruction;

import java.util.Collections;
import java.util.List;

import io.reactivex.disposables.Disposable;

public final class BroadcastReceiverProcess {
    private final EventBus eventBus;
    private final Dispatcher<NPCommand> dispatcher;
    private final Gson gson = new Gson();

    @Nullable
    private Disposable disposable;

    public static class BroadcastEvent implements EventBus.Event {
        final String data;
        final String body;

        public BroadcastEvent(String data, String body) {
            this.data = data;
            this.body = body;
        }
    }

    public BroadcastReceiverProcess(@NonNull Dispatcher<NPCommand> dispatcher,
                                    @NonNull EventBus eventBus) {
        this.dispatcher = dispatcher;
        this.eventBus = eventBus;
    }

    private final class Data {

        @Nullable
        @SerializedName("id")
        final String id;

        @Nullable
        @SerializedName("status")
        final Integer status;

        private Data(@Nullable String id, @Nullable Integer status) {
            this.id = id;
            this.status = status;
        }
    }

    private void dispose() {
        RxUtils.dispose(disposable);
    }

    public void execute() {
        dispose();
        disposable = eventBus.observeEvents()
                .ofType(BroadcastEvent.class)
                .subscribe(this::handleEvent, throwable ->
                        NPLogger.logError("BroadcastReceiver#observeEvents", throwable));
    }

    private void handleEvent(BroadcastEvent event) {
        NPLogger.log("Handling intent, data: " + event.data + ", body: " + event.body);

        try {
            final Data data = gson.fromJson(event.data, Data.class);
            if (data.id == null) {
                throw new NullPointerException("data.id should not be null");
            }

            final Instruction instruction = new Instruction(data.id, new Instruction.Input(data.status, event.body));
            final List<Instruction> instructions = Collections.singletonList(instruction);
            final NPCommand command = new NPCommand.ApplyInstructions(instructions);

            dispatcher.dispatch(command);
        } catch (Exception e) {
            NPLogger.logError("handleIntent", e);
        }
    }
}

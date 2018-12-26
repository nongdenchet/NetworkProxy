package com.rain.networkproxy.helper;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.rain.networkproxy.NPCommand;
import com.rain.networkproxy.internal.Dispatcher;
import com.rain.networkproxy.model.Instruction;

import java.util.Collections;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import static com.rain.networkproxy.Constants.INSTRUCTION_EVENT;
import static com.rain.networkproxy.Constants.INSTRUCTION_EVENT_BODY;
import static com.rain.networkproxy.Constants.INSTRUCTION_EVENT_DATA;

public final class BroadcastReceiverProcess {
    private final Context context;
    private final Dispatcher<NPCommand> dispatcher;
    private final Gson gson = new Gson();

    public BroadcastReceiverProcess(@NonNull Dispatcher<NPCommand> dispatcher,
                                    @NonNull Context context) {
        this.dispatcher = dispatcher;
        this.context = context;
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

    public void execute() {
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        handleIntent(intent);
                    }
                }, new IntentFilter(INSTRUCTION_EVENT));
    }

    private void handleIntent(Intent intent) {
        final String rawData = intent.getStringExtra(INSTRUCTION_EVENT_DATA);
        final String body = intent.getStringExtra(INSTRUCTION_EVENT_BODY);

        NPLogger.log("Handling intent, data: " + rawData + ", body: " + body);

        try {
            final Data data = gson.fromJson(rawData, Data.class);
            if (data.id == null) {
                throw new NullPointerException("data.id should not be null");
            }

            final Instruction instruction = new Instruction(data.id, new Instruction.Input(data.status, body));
            final List<Instruction> instructions = Collections.singletonList(instruction);
            final NPCommand command = new NPCommand.ApplyInstructions(instructions);

            dispatcher.dispatch(command);
        } catch (Exception e) {
            NPLogger.logError("handleIntent", e);
        }
    }
}

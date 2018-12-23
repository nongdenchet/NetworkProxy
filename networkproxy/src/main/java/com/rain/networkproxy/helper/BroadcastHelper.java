package com.rain.networkproxy.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.rain.networkproxy.NPCommand;
import com.rain.networkproxy.internal.Dispatcher;
import com.rain.networkproxy.model.Instruction;

import java.util.Collections;
import java.util.List;

import static com.rain.networkproxy.Constants.INSTRUCTION_EVENT;
import static com.rain.networkproxy.Constants.INSTRUCTION_EVENT_DATA;

@SuppressWarnings("FieldCanBeLocal")
public final class BroadcastHelper {
    private final Dispatcher<NPCommand> dispatcher;
    private final Gson gson = new Gson();

    public BroadcastHelper(Dispatcher<NPCommand> dispatcher) {
        this.dispatcher = dispatcher;
    }

    private final class Data {
        @SerializedName("id")
        public final String id;

        private Data(String id) {
            this.id = id;
        }
    }

    public void execute(@NonNull Context context) {
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
        NPLogger.log("Handling intent, data: " + rawData);

        try {
            final Data data = gson.fromJson(rawData, Data.class);
            final Instruction instruction = new Instruction(data.id, new Instruction.Input());
            final List<Instruction> instructions = Collections.singletonList(instruction);
            final NPCommand command = new NPCommand.ApplyInstructions(instructions);

            dispatcher.dispatch(command);
        } catch (Exception e) {
            NPLogger.logError("handleIntent", e);
        }
    }
}

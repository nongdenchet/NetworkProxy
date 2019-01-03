package com.rain.networkproxy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rain.networkproxy.helper.BroadcastReceiverProcess;
import com.rain.networkproxy.helper.EventBus;

import static com.rain.networkproxy.Constants.INSTRUCTION_EVENT;
import static com.rain.networkproxy.Constants.INSTRUCTION_EVENT_BODY;
import static com.rain.networkproxy.Constants.INSTRUCTION_EVENT_DATA;

public final class NPReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (INSTRUCTION_EVENT.equals(intent.getAction())) {
            final EventBus.Event event = new BroadcastReceiverProcess.BroadcastEvent(
                    intent.getStringExtra(INSTRUCTION_EVENT_DATA),
                    intent.getStringExtra(INSTRUCTION_EVENT_BODY)
            );
            InstanceProvider.instance().provideEventBus()
                    .dispatch(event);
        }
    }
}

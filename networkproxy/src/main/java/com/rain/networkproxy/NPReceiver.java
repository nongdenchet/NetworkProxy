package com.rain.networkproxy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import static com.rain.networkproxy.Constants.INSTRUCTION_EVENT;
import static com.rain.networkproxy.Constants.INSTRUCTION_EVENT_BODY;
import static com.rain.networkproxy.Constants.INSTRUCTION_EVENT_DATA;

public final class NPReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final Intent newIntent = new Intent(INSTRUCTION_EVENT);
        newIntent.putExtra(INSTRUCTION_EVENT_DATA, intent.getStringExtra(INSTRUCTION_EVENT_DATA));
        newIntent.putExtra(INSTRUCTION_EVENT_BODY, intent.getStringExtra(INSTRUCTION_EVENT_BODY));
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(newIntent);
    }
}

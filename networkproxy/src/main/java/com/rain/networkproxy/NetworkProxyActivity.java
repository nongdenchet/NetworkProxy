package com.rain.networkproxy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public final class NetworkProxyActivity extends AppCompatActivity {

    static Intent newIntent(@NonNull Context context) {
        return new Intent(context, NetworkProxyActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_proxy);
    }
}

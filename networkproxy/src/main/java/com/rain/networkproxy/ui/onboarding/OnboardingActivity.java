package com.rain.networkproxy.ui.onboarding;

import com.rain.networkproxy.R;
import com.rain.networkproxy.ui.dashboard.Dashboard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public final class OnboardingActivity extends AppCompatActivity {
    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, OnboardingActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network_proxy_activity_onboarding);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this, Dashboard.class));
        finish();
    }
}

package com.rain.networkproxy.ui.onboarding;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import com.rain.networkproxy.ui.Utils;
import com.rain.networkproxy.ui.dashboard.Dashboard;

public final class OnboardingActivity extends AppCompatActivity {
    private boolean askedPermission = false;

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, OnboardingActivity.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.hasOverlayPermission(this)) {
            startService(new Intent(this, Dashboard.class));
            finish();
        } else if (!askedPermission) {
            Utils.toOverlayPermission(this);
            askedPermission = true;
        } else {
            finish();
        }
    }
}

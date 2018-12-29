package com.rain.networkproxy.helper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

public final class ResourceProviderImpl implements ResourceProvider {
    private final Context context;

    public ResourceProviderImpl(@NonNull Context context) {
        this.context = context;
    }

    public String getString(@StringRes int id) {
        return context.getString(id);
    }
}

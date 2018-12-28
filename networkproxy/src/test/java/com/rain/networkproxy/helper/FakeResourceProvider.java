package com.rain.networkproxy.helper;

import android.content.Context;

import static org.mockito.Mockito.mock;

public class FakeResourceProvider extends ResourceProvider {

    public FakeResourceProvider() {
        super(mock(Context.class));
    }

    @Override
    public String getString(int id) {
        return String.valueOf(id);
    }
}

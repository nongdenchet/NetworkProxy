package com.rain.networkproxy.helper;

public final class FakeResourceProvider implements ResourceProvider {

    @Override
    public String getString(int id) {
        return String.valueOf(id);
    }
}

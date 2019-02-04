package com.rain.networkproxy.setting;

import java.util.prefs.Preferences;

public final class SettingStorage {
    private final static String PORT = "PORT";

    private final Preferences preferences = Preferences.userRoot().node(this.getClass().getName());

    public void setPort(int port) {
        preferences.putInt(PORT, port);
    }

    public int getPort() {
        return preferences.getInt(PORT, 8000);
    }
}

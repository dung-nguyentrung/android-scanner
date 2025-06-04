package com.harry.uhf_c.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsStorage {
    private static final String PREF_NAME = "app_settings";
    private final SharedPreferences prefs;

    public SettingsStorage(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveAntenna(int index, boolean enabled, int power) {
        prefs.edit()
                .putBoolean("ant_enabled_" + index, enabled)
                .putInt("ant_power_" + index, power)
                .apply();
    }

    public boolean isAntennaEnabled(int index) {
        return prefs.getBoolean("ant_enabled_" + index, false);
    }

    public int getAntennaPower(int index) {
        return prefs.getInt("ant_power_" + index, 30);
    }

    public void saveApiAddress(String url) {
        prefs.edit().putString("api_address", url).apply();
    }

    public String getApiAddress() {
        return prefs.getString("api_address", "");
    }

    public void saveDeviceSn(String sn) {
        prefs.edit().putString("device_sn", sn).apply();
    }

    public String getDeviceSn() {
        return prefs.getString("device_sn", "");
    }
}

package com.example.clot.models;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class PinStorage {
    private static final String PREFS_NAME = "pin_storage";
    private final SharedPreferences prefs;

    public PinStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    public void savePin(String userId, String pin) {
        prefs.edit().putString("pin_" + userId, pin).apply();
    }

    public String getPin(String userId) {
        return prefs.getString("pin_" + userId, null);
    }

    public boolean hasPin(String userId) {
        return prefs.contains("pin_" + userId);
    }
}
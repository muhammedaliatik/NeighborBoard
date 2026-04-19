package com.muhammedaliatik.neighborboard.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "NeighborBoardSession";
    private static final String KEY_UID = "uid";
    private static final String KEY_DISPLAY_NAME = "displayName";
    private static final String KEY_APARTMENT_CODE = "apartmentCode";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(String uid, String displayName, String apartmentCode) {
        editor.putString(KEY_UID, uid);
        editor.putString(KEY_DISPLAY_NAME, displayName);
        editor.putString(KEY_APARTMENT_CODE, apartmentCode);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUid() {
        return prefs.getString(KEY_UID, null);
    }

    public String getDisplayName() {
        return prefs.getString(KEY_DISPLAY_NAME, null);
    }

    public String getApartmentCode() {
        return prefs.getString(KEY_APARTMENT_CODE, null);
    }
}
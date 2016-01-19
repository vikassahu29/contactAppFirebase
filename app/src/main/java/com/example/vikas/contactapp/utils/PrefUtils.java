package com.example.vikas.contactapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by vikas on 19/1/16.
 */
public class PrefUtils {

    public static final String PREF_IS_LOGGED_IN = "pref_logged_in";
    public static final String PREF_UID = "pref_uid";

    public static boolean isLoggedIn(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_IS_LOGGED_IN, false);
    }

    public static String getUid(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_UID, "");
    }
}

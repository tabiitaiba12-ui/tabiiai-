package com.tabi.ai.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Thin wrapper around SharedPreferences for simple app settings:
 * dark mode preference and (optionally) user-supplied API keys.
 */
public final class PreferenceHelper {

    private PreferenceHelper() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static int getDarkModeSetting(Context context) {
        return prefs(context).getInt(Constants.PREF_DARK_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public static void setDarkModeSetting(Context context, int mode) {
        prefs(context).edit().putInt(Constants.PREF_DARK_MODE, mode).apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public static void toggleDarkMode(Context context) {
        boolean isCurrentlyDark = isNightModeActive(context);
        int newMode = isCurrentlyDark ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
        setDarkModeSetting(context, newMode);
    }

    private static boolean isNightModeActive(Context context) {
        int currentMode = context.getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        return currentMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }

    public static String getOpenAiKey(Context context) {
        return prefs(context).getString(Constants.PREF_OPENAI_KEY, "");
    }

    public static void setOpenAiKey(Context context, String key) {
        prefs(context).edit().putString(Constants.PREF_OPENAI_KEY, key).apply();
    }

    public static String getOpenWeatherKey(Context context) {
        return prefs(context).getString(Constants.PREF_OPENWEATHER_KEY, "");
    }

    public static void setOpenWeatherKey(Context context, String key) {
        prefs(context).edit().putString(Constants.PREF_OPENWEATHER_KEY, key).apply();
    }
}

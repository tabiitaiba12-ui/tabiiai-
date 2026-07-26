package com.tabi.ai;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import com.tabi.ai.data.local.AppDatabase;
import com.tabi.ai.utils.PreferenceHelper;

/**
 * Application entry point. Initializes the database singleton and
 * restores the user's preferred theme (light / dark / system) on cold start.
 */
public class TabiApplication extends Application {

    private AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();

        database = AppDatabase.getInstance(this);

        int nightMode = PreferenceHelper.getDarkModeSetting(this);
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    public AppDatabase getDatabase() {
        return database;
    }
}

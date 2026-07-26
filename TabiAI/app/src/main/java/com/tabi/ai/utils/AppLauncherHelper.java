package com.tabi.ai.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.List;

/**
 * Handles launching installed apps and system intents (browser search,
 * YouTube search, camera) on behalf of the assistant.
 */
public class AppLauncherHelper {

    private final Context context;

    public AppLauncherHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Attempts to find an installed app whose label loosely matches
     * {@code appName} and launch it. Returns true if a match was launched.
     */
    public boolean openAppByName(String appName) {
        if (appName == null || appName.trim().isEmpty()) {
            return false;
        }
        String normalizedTarget = appName.trim().toLowerCase();

        PackageManager packageManager = context.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<android.content.pm.ResolveInfo> resolveInfos = packageManager.queryIntentActivities(mainIntent, 0);

        for (android.content.pm.ResolveInfo info : resolveInfos) {
            String label = info.loadLabel(packageManager).toString().toLowerCase();
            if (label.contains(normalizedTarget) || normalizedTarget.contains(label)) {
                String packageName = info.activityInfo.packageName;
                Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(launchIntent);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isAppInstalled(String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void googleSearch(String query) {
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(android.app.SearchManager.QUERY, query);
        intent.setPackage("com.google.android.googlequicksearchbox");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            // Fallback to browser search
            Uri uri = Uri.parse("https://www.google.com/search?q=" + Uri.encode(query));
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(browserIntent);
        }
    }

    public void youTubeSearch(String query) {
        Uri uri = Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(query));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    public List<ApplicationInfo> getInstalledApps() {
        return context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
    }
}

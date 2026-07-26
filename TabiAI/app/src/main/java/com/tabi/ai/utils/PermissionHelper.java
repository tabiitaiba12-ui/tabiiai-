package com.tabi.ai.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Centralizes the runtime permissions Tabi AI needs and helpers to
 * request / check them.
 */
public final class PermissionHelper {

    private PermissionHelper() {
    }

    public static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static boolean hasAllPermissions(Context context) {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestAllPermissions(Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity, REQUIRED_PERMISSIONS, requestCode);
    }

    public static boolean hasMicrophonePermission(Context context) {
        return hasPermission(context, Manifest.permission.RECORD_AUDIO);
    }

    public static boolean hasCameraPermission(Context context) {
        return hasPermission(context, Manifest.permission.CAMERA);
    }

    public static boolean hasLocationPermission(Context context) {
        return hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                || hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
    }
}

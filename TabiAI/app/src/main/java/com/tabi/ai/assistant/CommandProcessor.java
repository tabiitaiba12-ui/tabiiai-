package com.tabi.ai.assistant;

import android.content.Context;

import com.tabi.ai.data.repository.WeatherRepository;
import com.tabi.ai.utils.AppLauncherHelper;
import com.tabi.ai.utils.DateTimeHelper;
import com.tabi.ai.utils.FlashlightHelper;
import com.tabi.ai.utils.PermissionHelper;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Inspects user input for on-device "skills" (open app, search, camera,
 * flashlight, time/date, weather) before falling back to the OpenAI model
 * for general conversation. This keeps simple device actions instant and
 * offline where possible.
 */
public class CommandProcessor {

    /** Result of trying to handle a command locally. */
    public static class CommandResult {
        public final boolean handled;
        public final String spokenResponse;

        private CommandResult(boolean handled, String spokenResponse) {
            this.handled = handled;
            this.spokenResponse = spokenResponse;
        }

        static CommandResult handled(String response) {
            return new CommandResult(true, response);
        }

        static CommandResult notHandled() {
            return new CommandResult(false, null);
        }
    }

    public interface WeatherResultCallback {
        void onWeatherResult(String spokenResponse);
    }

    private final Context context;
    private final AppLauncherHelper appLauncherHelper;
    private final FlashlightHelper flashlightHelper;
    private final WeatherRepository weatherRepository;

    private static final Pattern OPEN_APP_PATTERN = Pattern.compile(
            "(?:open|launch|start)\\s+(.+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern GOOGLE_SEARCH_PATTERN = Pattern.compile(
            "(?:google|search google for|search for|look up)\\s+(.+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern YOUTUBE_SEARCH_PATTERN = Pattern.compile(
            "(?:youtube|search youtube for|play on youtube|find on youtube)\\s+(.+)", Pattern.CASE_INSENSITIVE);

    public CommandProcessor(Context context) {
        this.context = context.getApplicationContext();
        this.appLauncherHelper = new AppLauncherHelper(context);
        this.flashlightHelper = new FlashlightHelper(context);
        this.weatherRepository = new WeatherRepository(context);
    }

    /**
     * Tries to resolve {@code input} as a local device command.
     * Weather requires an async lookup, so it is handled separately via
     * {@link #tryHandleWeather(String, WeatherResultCallback)}.
     */
    public CommandResult tryHandle(String input) {
        if (input == null || input.trim().isEmpty()) {
            return CommandResult.notHandled();
        }
        String normalized = input.trim().toLowerCase(Locale.getDefault());

        // Time
        if (normalized.matches(".*(what('| i)?s the time|current time|tell me the time).*")) {
            return CommandResult.handled(DateTimeHelper.getCurrentTimeSpoken());
        }

        // Date
        if (normalized.matches(".*(what('| i)?s the date|today'?s date|what day is it).*")) {
            return CommandResult.handled(DateTimeHelper.getCurrentDateSpoken());
        }

        // Flashlight
        if (normalized.contains("flashlight") || normalized.contains("torch")) {
            if (normalized.contains("off") || normalized.contains("turn off")) {
                if (PermissionHelper.hasCameraPermission(context)) {
                    flashlightHelper.setTorch(false);
                    return CommandResult.handled("Flashlight turned off");
                }
                return CommandResult.handled("I need camera permission to control the flashlight");
            } else {
                if (PermissionHelper.hasCameraPermission(context)) {
                    if (!flashlightHelper.hasFlashlight()) {
                        return CommandResult.handled("This device doesn't seem to have a flashlight");
                    }
                    boolean nowOn = flashlightHelper.toggle();
                    return CommandResult.handled(nowOn ? "Flashlight turned on" : "Flashlight turned off");
                }
                return CommandResult.handled("I need camera permission to control the flashlight");
            }
        }

        // Camera
        if (normalized.contains("open camera") || normalized.equals("camera") || normalized.contains("take a photo") || normalized.contains("take a picture")) {
            if (PermissionHelper.hasCameraPermission(context)) {
                appLauncherHelper.openCamera();
                return CommandResult.handled("Opening the camera");
            }
            return CommandResult.handled("I need camera permission first");
        }

        // YouTube search (checked before Google, since "search youtube for" contains "search for")
        Matcher youtubeMatcher = YOUTUBE_SEARCH_PATTERN.matcher(normalized);
        if (youtubeMatcher.find()) {
            String query = youtubeMatcher.group(1).trim();
            appLauncherHelper.youTubeSearch(query);
            return CommandResult.handled("Searching YouTube for " + query);
        }

        // Google search
        Matcher googleMatcher = GOOGLE_SEARCH_PATTERN.matcher(normalized);
        if (googleMatcher.find()) {
            String query = googleMatcher.group(1).trim();
            appLauncherHelper.googleSearch(query);
            return CommandResult.handled("Searching Google for " + query);
        }

        // Open app
        Matcher openAppMatcher = OPEN_APP_PATTERN.matcher(normalized);
        if (openAppMatcher.find()) {
            String appName = openAppMatcher.group(1).trim();
            // avoid false positives like "open camera" (handled above) already excluded by ordering
            boolean launched = appLauncherHelper.openAppByName(appName);
            if (launched) {
                return CommandResult.handled("Opening " + appName);
            } else {
                return CommandResult.handled("Sorry, I couldn't find an app called " + appName);
            }
        }

        return CommandResult.notHandled();
    }

    /**
     * Weather lookups are async. Returns true if the input looked like a
     * weather request (in which case {@code callback} will eventually fire).
     */
    public boolean tryHandleWeather(String input, double lat, double lon, WeatherResultCallback callback) {
        String normalized = input.trim().toLowerCase(Locale.getDefault());
        if (!normalized.contains("weather")) {
            return false;
        }

        if (!PermissionHelper.hasLocationPermission(context)) {
            callback.onWeatherResult("I need location permission to check the weather");
            return true;
        }

        weatherRepository.getWeatherByCoordinates(lat, lon, new WeatherRepository.WeatherCallback() {
            @Override
            public void onSuccess(String humanReadableSummary) {
                callback.onWeatherResult(humanReadableSummary);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onWeatherResult("I couldn't fetch the weather right now: " + errorMessage);
            }
        });
        return true;
    }
}

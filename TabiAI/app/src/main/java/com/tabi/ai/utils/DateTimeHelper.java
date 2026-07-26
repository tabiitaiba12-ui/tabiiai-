package com.tabi.ai.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Small helper for formatting the current time, date, and chat bubble timestamps.
 */
public final class DateTimeHelper {

    private DateTimeHelper() {
    }

    public static String getCurrentTimeSpoken() {
        SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return "It's currently " + format.format(new Date());
    }

    public static String getCurrentDateSpoken() {
        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        return "Today is " + format.format(new Date());
    }

    public static String getBubbleTimestamp(long timestampMillis) {
        SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return format.format(new Date(timestampMillis));
    }

    public static String getGreetingByTimeOfDay() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) {
            return "Good morning";
        } else if (hour < 18) {
            return "Good afternoon";
        } else {
            return "Good evening";
        }
    }
}

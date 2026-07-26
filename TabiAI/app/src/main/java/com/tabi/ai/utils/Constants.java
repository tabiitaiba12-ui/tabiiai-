package com.tabi.ai.utils;

/**
 * App-wide constant values.
 */
public final class Constants {

    private Constants() {
        // no instances
    }

    // Networking
    public static final String OPENAI_BASE_URL = "https://api.openai.com/";
    public static final String OPENAI_CHAT_MODEL = "gpt-4o-mini";
    public static final String OPENWEATHER_BASE_URL = "https://api.openweathermap.org/";

    // Database
    public static final String DATABASE_NAME = "tabi_ai_db";
    public static final int DATABASE_VERSION = 1;

    // Preferences
    public static final String PREFS_NAME = "tabi_ai_prefs";
    public static final String PREF_DARK_MODE = "pref_dark_mode";
    public static final String PREF_OPENAI_KEY = "pref_openai_key";
    public static final String PREF_OPENWEATHER_KEY = "pref_openweather_key";

    // Request codes
    public static final int REQUEST_CODE_PERMISSIONS = 1001;
    public static final int REQUEST_CODE_SPEECH_INPUT = 1002;

    // Sender types
    public static final int SENDER_USER = 0;
    public static final int SENDER_AI = 1;

    // System prompt sent to OpenAI so the model behaves like a voice assistant
    public static final String SYSTEM_PROMPT =
            "You are Tabi, a helpful, friendly, and concise voice assistant living inside an Android app. " +
                    "Keep answers short and conversational since they may be read aloud with text-to-speech. " +
                    "Avoid long lists or markdown formatting unless explicitly asked.";
}

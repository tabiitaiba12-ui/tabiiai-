package com.tabi.ai.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;
import java.util.UUID;

/**
 * Wraps {@link TextToSpeech} to speak Tabi's replies aloud and notify
 * the caller when speech starts/finishes, so the UI can, for example,
 * automatically resume listening for the next voice command.
 */
public class TextToSpeechHelper {

    public interface Listener {
        void onTtsReady();
        void onSpeechStarted();
        void onSpeechFinished();
        void onTtsError(String message);
    }

    private TextToSpeech textToSpeech;
    private final Listener listener;
    private boolean isReady = false;

    public TextToSpeechHelper(Context context, Listener listener) {
        this.listener = listener;
        textToSpeech = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.getDefault());
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    textToSpeech.setLanguage(Locale.US);
                }
                textToSpeech.setPitch(1.0f);
                textToSpeech.setSpeechRate(1.0f);
                isReady = true;
                listener.onTtsReady();
            } else {
                listener.onTtsError("Text-to-speech engine failed to initialize");
            }
        });

        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                listener.onSpeechStarted();
            }

            @Override
            public void onDone(String utteranceId) {
                listener.onSpeechFinished();
            }

            @Override
            public void onError(String utteranceId) {
                listener.onTtsError("Error while speaking");
            }
        });
    }

    public void speak(String text) {
        if (!isReady || text == null || text.trim().isEmpty()) {
            return;
        }
        String utteranceId = UUID.randomUUID().toString();
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    public void stop() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    public boolean isSpeaking() {
        return textToSpeech != null && textToSpeech.isSpeaking();
    }

    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}

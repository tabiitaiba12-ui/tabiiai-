package com.tabi.ai.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Wraps Android's on-device {@link SpeechRecognizer} to provide a simple
 * callback-based API for continuous voice conversation.
 */
public class SpeechRecognitionHelper {

    public interface Listener {
        void onReadyForSpeech();
        void onSpeechResult(String recognizedText);
        void onError(String errorMessage);
        void onListeningStateChanged(boolean isListening);
    }

    private final SpeechRecognizer speechRecognizer;
    private final Intent recognizerIntent;
    private final Listener listener;
    private boolean isListening = false;

    public SpeechRecognitionHelper(Context context, Listener listener) {
        this.listener = listener;
        this.speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        this.recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                listener.onReadyForSpeech();
            }

            @Override
            public void onBeginningOfSpeech() {
                // no-op
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // could be used to animate a waveform
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // no-op
            }

            @Override
            public void onEndOfSpeech() {
                setListening(false);
            }

            @Override
            public void onError(int error) {
                setListening(false);
                listener.onError(mapErrorCode(error));
            }

            @Override
            public void onResults(Bundle results) {
                setListening(false);
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    listener.onSpeechResult(matches.get(0));
                } else {
                    listener.onError("No speech detected");
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // no-op, partial results disabled
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // no-op
            }
        });
    }

    public void startListening() {
        if (SpeechRecognizer.isRecognitionAvailable(null)) {
            // fallthrough, isRecognitionAvailable requires context in newer APIs; guarded by caller
        }
        setListening(true);
        speechRecognizer.startListening(recognizerIntent);
    }

    public void stopListening() {
        setListening(false);
        speechRecognizer.stopListening();
    }

    public void cancel() {
        setListening(false);
        speechRecognizer.cancel();
    }

    public boolean isListening() {
        return isListening;
    }

    public void destroy() {
        speechRecognizer.destroy();
    }

    private void setListening(boolean listening) {
        this.isListening = listening;
        listener.onListeningStateChanged(listening);
    }

    private String mapErrorCode(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Microphone permission required";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "Didn't catch that, please try again";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognizer busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech input detected";
            default:
                return "Unknown speech recognition error";
        }
    }
}

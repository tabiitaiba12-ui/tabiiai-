package com.tabi.ai;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tabi.ai.assistant.CommandProcessor;
import com.tabi.ai.data.local.ChatMessageEntity;
import com.tabi.ai.ui.adapter.ChatAdapter;
import com.tabi.ai.ui.main.MainViewModel;
import com.tabi.ai.utils.PermissionHelper;
import com.tabi.ai.utils.SpeechRecognitionHelper;
import com.tabi.ai.utils.TextToSpeechHelper;

import java.util.List;
import java.util.Locale;

import android.widget.EditText;
import android.widget.TextView;

/**
 * Single-screen voice assistant UI. Wires together speech recognition,
 * text-to-speech, the on-device CommandProcessor, and the MainViewModel
 * (which talks to OpenAI + Room) into one continuous conversation loop.
 */
public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private ChatAdapter chatAdapter;
    private CommandProcessor commandProcessor;
    private SpeechRecognitionHelper speechRecognitionHelper;
    private TextToSpeechHelper textToSpeechHelper;

    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnMic;
    private ImageButton btnSend;
    private ImageButton btnClearChat;
    private ImageButton btnSettings;
    private TextView tvStatus;
    private TextView tvEmptyState;

    /** When true, Tabi automatically starts listening again after speaking a reply. */
    private boolean continuousConversationMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.core.splashscreen.SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        setupRecyclerView();
        setupViewModel();
        setupHelpers();
        setupListeners();

        if (!PermissionHelper.hasAllPermissions(this)) {
            requestNeededPermissions();
        }
    }

    private void bindViews() {
        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnMic = findViewById(R.id.btnMic);
        btnSend = findViewById(R.id.btnSend);
        btnClearChat = findViewById(R.id.btnClearChat);
        btnSettings = findViewById(R.id.btnSettings);
        tvStatus = findViewById(R.id.tvStatus);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(chatAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.getChatHistory().observe(this, this::onChatHistoryChanged);

        viewModel.getIsThinking().observe(this, thinking -> {
            if (Boolean.TRUE.equals(thinking)) {
                showStatus(getString(R.string.thinking));
            } else {
                hideStatus();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getLastAiReply().observe(this, reply -> {
            if (reply != null && !reply.isEmpty()) {
                speakAndMaybeContinue(reply);
            }
        });
    }

    private void setupHelpers() {
        commandProcessor = new CommandProcessor(this);

        speechRecognitionHelper = new SpeechRecognitionHelper(this, new SpeechRecognitionHelper.Listener() {
            @Override
            public void onReadyForSpeech() {
                showStatus(getString(R.string.listening));
            }

            @Override
            public void onSpeechResult(String recognizedText) {
                hideStatus();
                handleUserInput(recognizedText);
            }

            @Override
            public void onError(String errorMessage) {
                hideStatus();
                setMicListeningUi(false);
                if (continuousConversationMode) {
                    // Silently stop the loop on repeated errors rather than spamming toasts
                    continuousConversationMode = false;
                } else {
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onListeningStateChanged(boolean isListening) {
                setMicListeningUi(isListening);
            }
        });

        textToSpeechHelper = new TextToSpeechHelper(this, new TextToSpeechHelper.Listener() {
            @Override
            public void onTtsReady() {
                // ready
            }

            @Override
            public void onSpeechStarted() {
                // could animate a "speaking" indicator here
            }

            @Override
            public void onSpeechFinished() {
                if (continuousConversationMode) {
                    startListeningIfPermitted();
                }
            }

            @Override
            public void onTtsError(String message) {
                // fail silently, text is still shown in chat
            }
        });
    }

    private void setupListeners() {
        btnMic.setOnClickListener(v -> onMicButtonClicked());

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                continuousConversationMode = false;
                etMessage.setText("");
                handleUserInput(text);
            }
        });

        btnClearChat.setOnClickListener(v -> confirmClearHistory());

        btnSettings.setOnClickListener(v -> startActivity(
                new android.content.Intent(MainActivity.this, SettingsActivity.class)));
    }

    private void onMicButtonClicked() {
        if (speechRecognitionHelper.isListening()) {
            speechRecognitionHelper.stopListening();
            continuousConversationMode = false;
            return;
        }
        continuousConversationMode = true;
        startListeningIfPermitted();
    }

    private void startListeningIfPermitted() {
        if (!PermissionHelper.hasMicrophonePermission(this)) {
            requestNeededPermissions();
            return;
        }
        speechRecognitionHelper.startListening();
    }

    /**
     * Central entry point for anything the user says or types. First checks
     * whether it's a device command (open app, flashlight, weather, etc.),
     * and only falls back to the AI model for general conversation.
     */
    private void handleUserInput(String input) {
        viewModel.saveUserMessage(input);

        CommandProcessor.CommandResult localResult = commandProcessor.tryHandle(input);
        if (localResult.handled) {
            viewModel.saveAiMessage(localResult.spokenResponse);
            speakAndMaybeContinue(localResult.spokenResponse);
            return;
        }

        boolean isWeatherRequest = tryHandleWeatherWithLocation(input);
        if (isWeatherRequest) {
            return;
        }

        // Fall back to the AI model for general conversation
        viewModel.sendToAi(input);
    }

    @SuppressLint("MissingPermission")
    private boolean tryHandleWeatherWithLocation(String input) {
        if (!input.toLowerCase(Locale.getDefault()).contains("weather")) {
            return false;
        }

        if (!PermissionHelper.hasLocationPermission(this)) {
            String reply = "I need location permission to check the weather";
            viewModel.saveAiMessage(reply);
            speakAndMaybeContinue(reply);
            return true;
        }

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location lastKnown = getBestLastKnownLocation(locationManager);

        double lat = lastKnown != null ? lastKnown.getLatitude() : 0;
        double lon = lastKnown != null ? lastKnown.getLongitude() : 0;

        if (lastKnown == null) {
            String reply = "I couldn't determine your location right now";
            viewModel.saveAiMessage(reply);
            speakAndMaybeContinue(reply);
            return true;
        }

        showStatus(getString(R.string.thinking));
        commandProcessor.tryHandleWeather(input, lat, lon, spokenResponse -> runOnUiThread(() -> {
            hideStatus();
            viewModel.saveAiMessage(spokenResponse);
            speakAndMaybeContinue(spokenResponse);
        }));
        return true;
    }

    @SuppressLint("MissingPermission")
    private Location getBestLastKnownLocation(LocationManager locationManager) {
        if (locationManager == null) {
            return null;
        }
        Location best = null;
        for (String provider : locationManager.getProviders(true)) {
            try {
                Location candidate = locationManager.getLastKnownLocation(provider);
                if (candidate != null && (best == null || candidate.getAccuracy() < best.getAccuracy())) {
                    best = candidate;
                }
            } catch (SecurityException ignored) {
                // permission already checked by caller
            }
        }
        return best;
    }

    private void speakAndMaybeContinue(String text) {
        textToSpeechHelper.speak(text);
    }

    private void onChatHistoryChanged(List<ChatMessageEntity> messages) {
        chatAdapter.submitList(messages);
        tvEmptyState.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
        if (!messages.isEmpty()) {
            rvChat.scrollToPosition(messages.size() - 1);
        }
    }

    private void confirmClearHistory() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.menu_clear_chat)
                .setMessage("This will permanently delete your chat history. Continue?")
                .setPositiveButton("Clear", (dialog, which) -> viewModel.clearHistory())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void setMicListeningUi(boolean isListening) {
        btnMic.setImageResource(R.drawable.ic_mic);
        btnMic.setActivated(isListening);
    }

    private void showStatus(String text) {
        tvStatus.setText(text);
        tvStatus.setVisibility(View.VISIBLE);
    }

    private void hideStatus() {
        tvStatus.setVisibility(View.INVISIBLE);
    }

    private void requestNeededPermissions() {
        PermissionHelper.requestAllPermissions(this, com.tabi.ai.utils.Constants.REQUEST_CODE_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == com.tabi.ai.utils.Constants.REQUEST_CODE_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                Toast.makeText(this, R.string.permission_rationale_message, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        speechRecognitionHelper.destroy();
        textToSpeechHelper.shutdown();
        super.onDestroy();
    }
}

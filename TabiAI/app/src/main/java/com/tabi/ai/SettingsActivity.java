package com.tabi.ai;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.tabi.ai.utils.PreferenceHelper;

/**
 * Lets the user supply their own OpenAI / OpenWeatherMap API keys at runtime
 * (instead of baking them into the build) and toggle dark mode manually.
 */
public class SettingsActivity extends AppCompatActivity {

    private EditText etOpenAiKey;
    private EditText etWeatherKey;
    private SwitchMaterial switchDarkMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etOpenAiKey = findViewById(R.id.etOpenAiKey);
        etWeatherKey = findViewById(R.id.etWeatherKey);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        MaterialButton btnSave = findViewById(R.id.btnSave);

        etOpenAiKey.setText(PreferenceHelper.getOpenAiKey(this));
        etWeatherKey.setText(PreferenceHelper.getOpenWeatherKey(this));
        switchDarkMode.setChecked(isNightModeActive());

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int mode = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            PreferenceHelper.setDarkModeSetting(this, mode);
        });

        btnSave.setOnClickListener(v -> saveSettings());
    }

    private void saveSettings() {
        String openAiKey = etOpenAiKey.getText().toString().trim();
        String weatherKey = etWeatherKey.getText().toString().trim();

        PreferenceHelper.setOpenAiKey(this, openAiKey);
        PreferenceHelper.setOpenWeatherKey(this, weatherKey);

        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean isNightModeActive() {
        int currentMode = getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        return currentMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }
}

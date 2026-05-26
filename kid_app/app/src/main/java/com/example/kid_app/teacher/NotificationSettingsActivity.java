package com.example.kid_app.teacher;

import android.content.SharedPreferences;
import android.os.Bundle;
import com.example.kid_app.R;
import com.example.kid_app.common.BaseActivity;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class NotificationSettingsActivity extends BaseActivity {

    private SwitchMaterial switchSubmission, switchDeadline, switchFeedback, switchSound;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "notification_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        loadSettings();
        setupListeners();
    }

    private void initViews() {
        switchSubmission = findViewById(R.id.switch_submission);
        switchDeadline = findViewById(R.id.switch_deadline);
        switchFeedback = findViewById(R.id.switch_feedback);
        switchSound = findViewById(R.id.switch_sound);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void loadSettings() {
        switchSubmission.setChecked(prefs.getBoolean("notif_submission", true));
        switchDeadline.setChecked(prefs.getBoolean("notif_deadline", true));
        switchFeedback.setChecked(prefs.getBoolean("notif_feedback", true));
        switchSound.setChecked(prefs.getBoolean("notif_sound", true));
    }

    private void setupListeners() {
        switchSubmission.setOnCheckedChangeListener((buttonView, isChecked) -> 
                prefs.edit().putBoolean("notif_submission", isChecked).apply());

        switchDeadline.setOnCheckedChangeListener((buttonView, isChecked) -> 
                prefs.edit().putBoolean("notif_deadline", isChecked).apply());

        switchFeedback.setOnCheckedChangeListener((buttonView, isChecked) -> 
                prefs.edit().putBoolean("notif_feedback", isChecked).apply());

        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> 
                prefs.edit().putBoolean("notif_sound", isChecked).apply());
    }
}

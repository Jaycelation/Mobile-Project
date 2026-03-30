package com.example.kid_app.parent;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.mapper.DocumentMapper;
import com.example.kid_app.data.model.ChildProfile;
import com.example.kid_app.data.model.ChildSettings;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;

/**
 * ChildSettingsActivity — Cài đặt của phụ huynh cho từng bé.
 *
 * Nhận EXTRA: KEY_CHILD_ID (String)
 *
 * Cài đặt được lưu vào:
 *   /child_profiles/{childId}/settings/child_settings
 *
 * Ba cài đặt chính:
 * 1. daily_limit_minutes  — giới hạn thời gian mỗi ngày
 * 2. ai_enabled           — bật/tắt AI trợ lý
 * 3. content_age_filter   — lọc nội dung theo nhóm tuổi
 */
public class ChildSettingsActivity extends BaseActivity {

    private ChildProfileService childProfileService;

    private String childId;

    private TextView       tvChildNameHeader;
    private TextInputLayout tilDailyLimit;
    private SwitchMaterial  switchAi;
    private RadioGroup      rgContentFilter;
    private ProgressBar     progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_settings);

        childId = getIntent().getStringExtra(AppConstants.KEY_CHILD_ID);
        if (childId == null) {
            showToast("Lỗi: không tìm thấy ID bé");
            finish();
            return;
        }

        childProfileService = new ChildProfileService();

        bindViews();
        loadChildNameAndSettings();
    }

    private void bindViews() {
        tvChildNameHeader = findViewById(R.id.tv_child_name_header);
        tilDailyLimit     = findViewById(R.id.til_daily_limit);
        switchAi          = findViewById(R.id.switch_ai_enabled);
        rgContentFilter   = findViewById(R.id.rg_content_filter);
        progressBar       = findViewById(R.id.progress_bar);

        ImageButton    btnBack         = findViewById(R.id.btn_back);
        MaterialButton btnSaveSettings = findViewById(R.id.btn_save_settings);

        btnBack.setOnClickListener(v -> finish());
        btnSaveSettings.setOnClickListener(v -> attemptSave());
    }

    // ==================== LOAD ====================

    private void loadChildNameAndSettings() {
        showLoading(progressBar);

        // Load tên bé để hiển thị header
        childProfileService.getChildProfile(childId)
                .addOnSuccessListener(doc -> {
                    ChildProfile profile = DocumentMapper.toChildProfile(doc);
                    if (profile != null) {
                        String name = profile.getDisplayName();
                        tvChildNameHeader.setText("Bé: " + name);
                    }
                })
                .addOnFailureListener(e -> { /* không cần block */ });

        // Load settings
        childProfileService.getChildSettings(childId)
                .addOnSuccessListener(doc -> {
                    hideLoading(progressBar);
                    ChildSettings settings = DocumentMapper.toChildSettings(doc);
                    if (settings != null) {
                        prefillSettings(settings);
                    }
                    // Nếu null → dùng giá trị mặc định của form
                })
                .addOnFailureListener(e -> {
                    hideLoading(progressBar);
                    showToast("Không tải được cài đặt, dùng giá trị mặc định");
                });
    }

    private void prefillSettings(ChildSettings settings) {
        // Giới hạn thời gian
        if (tilDailyLimit.getEditText() != null) {
            tilDailyLimit.getEditText().setText(String.valueOf(settings.getDailyLimitMinutes()));
        }

        // AI switch
        switchAi.setChecked(settings.isAiEnabled());

        // Bộ lọc nội dung
        String filter = settings.getContentAgeFilter();
        if (AppConstants.AGE_GROUP_3_5.equals(filter)) {
            rgContentFilter.check(R.id.rb_filter_3_5);
        } else if (AppConstants.AGE_GROUP_9_12.equals(filter)) {
            rgContentFilter.check(R.id.rb_filter_9_12);
        } else {
            rgContentFilter.check(R.id.rb_filter_6_8); // default
        }
    }

    // ==================== SAVE ====================

    private void attemptSave() {
        tilDailyLimit.setError(null);

        // Parse daily limit
        String limitStr = "";
        if (tilDailyLimit.getEditText() != null) {
            limitStr = tilDailyLimit.getEditText().getText().toString().trim();
        }

        int dailyLimitMinutes = 60; // default
        if (!limitStr.isEmpty()) {
            try {
                dailyLimitMinutes = Integer.parseInt(limitStr);
                if (dailyLimitMinutes < 0 || dailyLimitMinutes > 1440) {
                    tilDailyLimit.setError("Nhập số phút hợp lệ (0–1440)");
                    return;
                }
            } catch (NumberFormatException e) {
                tilDailyLimit.setError("Vui lòng nhập số hợp lệ");
                return;
            }
        }

        boolean aiEnabled    = switchAi.isChecked();
        String  contentFilter = getSelectedFilter();

        ChildSettings settings = new ChildSettings(dailyLimitMinutes, aiEnabled, contentFilter);

        showLoading(progressBar);

        childProfileService.saveChildSettings(childId, settings)
                .addOnSuccessListener(unused -> {
                    hideLoading(progressBar);
                    showToast("✅ Đã lưu cài đặt cho bé");
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    hideLoading(progressBar);
                    showToast("Lưu thất bại: " + e.getMessage());
                });
    }

    // ==================== HELPERS ====================

    private String getSelectedFilter() {
        int id = rgContentFilter.getCheckedRadioButtonId();
        if (id == R.id.rb_filter_3_5)  return AppConstants.AGE_GROUP_3_5;
        if (id == R.id.rb_filter_9_12) return AppConstants.AGE_GROUP_9_12;
        return AppConstants.AGE_GROUP_6_8;
    }
}

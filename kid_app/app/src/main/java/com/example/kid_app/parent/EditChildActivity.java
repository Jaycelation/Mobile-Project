package com.example.kid_app.parent;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.mapper.DocumentMapper;
import com.example.kid_app.data.model.ChildProfile;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

/**
 * EditChildActivity — Chỉnh sửa hồ sơ bé đã có.
 *
 * Nhận EXTRA: KEY_CHILD_ID (String)
 * Flow:
 * 1. Load hồ sơ bé từ Firestore.
 * 2. Pre-fill vào form.
 * 3. Phụ huynh chỉnh sửa → save → finish().
 * 4. Hoặc xóa mềm → soft delete → finish().
 */
public class EditChildActivity extends BaseActivity {

    private ChildProfileService childProfileService;

    private String childId;
    private ChildProfile currentProfile;

    private TextInputLayout tilFullName;
    private TextInputLayout tilNickName;
    private TextInputLayout tilBirthDate;
    private RadioGroup rgGender;
    private RadioGroup rgAgeGroup;
    private ProgressBar progressBar;

    private String selectedBirthDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_child);

        childId = getIntent().getStringExtra(AppConstants.KEY_CHILD_ID);
        if (childId == null) {
            showToast("Lỗi: không tìm thấy ID bé");
            finish();
            return;
        }

        childProfileService = new ChildProfileService();
        bindViews();
        loadProfile();
    }

    private void bindViews() {
        tilFullName    = findViewById(R.id.til_full_name);
        tilNickName    = findViewById(R.id.til_nick_name);
        tilBirthDate   = findViewById(R.id.til_birth_date);
        rgGender       = findViewById(R.id.rg_gender);
        rgAgeGroup     = findViewById(R.id.rg_age_group);
        progressBar    = findViewById(R.id.progress_bar);

        ImageButton    btnBack   = findViewById(R.id.btn_back);
        MaterialButton btnSave   = findViewById(R.id.btn_save);
        MaterialButton btnDelete = findViewById(R.id.btn_delete);

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> attemptSave());
        btnDelete.setOnClickListener(v -> confirmDelete());

        if (tilBirthDate.getEditText() != null) {
            tilBirthDate.getEditText().setOnClickListener(v -> showDatePicker());
        }
    }

    // ==================== LOAD PROFILE ====================

    private void loadProfile() {
        showLoading(progressBar);
        childProfileService.getChildProfile(childId)
                .addOnSuccessListener(doc -> {
                    hideLoading(progressBar);
                    currentProfile = DocumentMapper.toChildProfile(doc);
                    if (currentProfile == null) {
                        showToast("Không tìm thấy hồ sơ bé");
                        finish();
                        return;
                    }
                    prefillForm(currentProfile);
                })
                .addOnFailureListener(e -> {
                    hideLoading(progressBar);
                    showToast("Lỗi tải hồ sơ: " + e.getMessage());
                    finish();
                });
    }

    private void prefillForm(ChildProfile profile) {
        if (tilFullName.getEditText() != null)
            tilFullName.getEditText().setText(profile.getFullName());
        if (tilNickName.getEditText() != null && profile.getNickName() != null)
            tilNickName.getEditText().setText(profile.getNickName());

        if (profile.getBirthDate() != null) {
            selectedBirthDate = profile.getBirthDate();
            if (tilBirthDate.getEditText() != null)
                tilBirthDate.getEditText().setText(selectedBirthDate);
        }

        // Giới tính
        if ("female".equals(profile.getGender())) {
            rgGender.check(R.id.rb_female);
        } else {
            rgGender.check(R.id.rb_male);
        }

        // Nhóm tuổi
        String ag = profile.getAgeGroup();
        if (AppConstants.AGE_GROUP_3_5.equals(ag)) {
            rgAgeGroup.check(R.id.rb_age_3_5);
        } else if (AppConstants.AGE_GROUP_9_12.equals(ag)) {
            rgAgeGroup.check(R.id.rb_age_9_12);
        } else {
            rgAgeGroup.check(R.id.rb_age_6_8);
        }

        // Avatar emoji
        updateAvatarEmoji(profile.getGender());
    }

    private void updateAvatarEmoji(String gender) {
        android.widget.TextView tvAvatar = findViewById(R.id.tv_avatar_preview);
        if (tvAvatar == null) return;
        tvAvatar.setText("female".equals(gender) ? "👧" : "👦");
    }

    // ==================== DATE PICKER ====================

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        if (!selectedBirthDate.isEmpty()) {
            try {
                String[] parts = selectedBirthDate.split("-");
                cal.set(Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]) - 1,
                        Integer.parseInt(parts[2]));
            } catch (Exception ignored) {}
        } else {
            cal.add(Calendar.YEAR, -6);
        }

        new DatePickerDialog(this,
                (view, year, month, day) -> {
                    selectedBirthDate = String.format("%04d-%02d-%02d", year, month + 1, day);
                    if (tilBirthDate.getEditText() != null)
                        tilBirthDate.getEditText().setText(selectedBirthDate);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    // ==================== SAVE ====================

    private void attemptSave() {
        tilFullName.setError(null);

        String fullName = getText(tilFullName);
        if (TextUtils.isEmpty(fullName) || fullName.length() < 2) {
            tilFullName.setError("Vui lòng nhập họ tên bé (tối thiểu 2 ký tự)");
            return;
        }

        String nickName = getText(tilNickName);
        String gender   = rgGender.getCheckedRadioButtonId() == R.id.rb_female ? "female" : "male";
        String ageGroup = getSelectedAgeGroup();

        ChildProfile updated = new ChildProfile(fullName, nickName, selectedBirthDate, gender, ageGroup);

        showLoading(progressBar);

        childProfileService.updateChildProfile(childId, updated)
                .addOnSuccessListener(unused -> {
                    hideLoading(progressBar);
                    showToast("✅ Đã cập nhật hồ sơ bé");
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    hideLoading(progressBar);
                    showToast("Lỗi: " + e.getMessage());
                });
    }

    // ==================== DELETE ====================

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa hồ sơ bé")
                .setMessage("Bạn có chắc muốn xóa hồ sơ này? Dữ liệu học tập sẽ không bị xóa.")
                .setPositiveButton("Xóa", (dialog, which) -> doSoftDelete())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void doSoftDelete() {
        showLoading(progressBar);
        new com.example.kid_app.data.repository.ChildProfileRepository()
                .softDeleteChildProfile(childId)
                .addOnSuccessListener(unused -> {
                    hideLoading(progressBar);
                    showToast("🗑️ Đã xóa hồ sơ bé");
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    hideLoading(progressBar);
                    showToast("Xóa thất bại: " + e.getMessage());
                });
    }

    // ==================== HELPERS ====================

    private String getText(TextInputLayout til) {
        if (til.getEditText() == null) return "";
        return til.getEditText().getText().toString().trim();
    }

    private String getSelectedAgeGroup() {
        int id = rgAgeGroup.getCheckedRadioButtonId();
        if (id == R.id.rb_age_3_5)  return AppConstants.AGE_GROUP_3_5;
        if (id == R.id.rb_age_9_12) return AppConstants.AGE_GROUP_9_12;
        return AppConstants.AGE_GROUP_6_8;
    }
}

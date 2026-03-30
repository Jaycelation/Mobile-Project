package com.example.kid_app.parent;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.auth.AuthService;
import com.example.kid_app.data.model.ChildProfile;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

/**
 * AddChildActivity — Form tạo hồ sơ bé mới.
 *
 * Flow:
 * 1. Phụ huynh điền thông tin bé (tên, ngày sinh, giới tính, nhóm tuổi, quan hệ).
 * 2. Validate dữ liệu.
 * 3. Gọi ChildProfileService.createChildProfile().
 * 4. Thành công → finish() về ParentHomeActivity.
 *
 * Kết quả: document /child_profiles/{childId}, /parent_child_links/{linkId},
 *          /child_stats/{childId}, settings với giá trị mặc định.
 */
public class AddChildActivity extends BaseActivity {

    private ChildProfileService childProfileService;
    private AuthService authService;

    private TextInputLayout tilFullName;
    private TextInputLayout tilNickName;
    private TextInputLayout tilBirthDate;
    private RadioGroup rgGender;
    private RadioGroup rgAgeGroup;
    private RadioGroup rgRelationship;
    private ProgressBar progressBar;

    private String selectedBirthDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);

        childProfileService = new ChildProfileService();
        authService         = new AuthService();

        bindViews();
    }

    private void bindViews() {
        tilFullName    = findViewById(R.id.til_full_name);
        tilNickName    = findViewById(R.id.til_nick_name);
        tilBirthDate   = findViewById(R.id.til_birth_date);
        rgGender       = findViewById(R.id.rg_gender);
        rgAgeGroup     = findViewById(R.id.rg_age_group);
        rgRelationship = findViewById(R.id.rg_relationship);
        progressBar    = findViewById(R.id.progress_bar);

        ImageButton    btnBack = findViewById(R.id.btn_back);
        MaterialButton btnSave = findViewById(R.id.btn_save);

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> attemptSave());

        // DatePicker cho ngày sinh
        if (tilBirthDate.getEditText() != null) {
            tilBirthDate.getEditText().setOnClickListener(v -> showDatePicker());
            tilBirthDate.setEndIconOnClickListener(v -> showDatePicker());
        }
    }

    // ==================== DATEPICKER ====================

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        // Default: 6 năm trước
        cal.add(Calendar.YEAR, -6);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    selectedBirthDate = String.format("%04d-%02d-%02d", year, month + 1, day);
                    if (tilBirthDate.getEditText() != null) {
                        tilBirthDate.getEditText().setText(selectedBirthDate);
                    }
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));

        // Không cho chọn ngày trong tương lai
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    // ==================== SAVE LOGIC ====================

    private void attemptSave() {
        clearErrors();

        String fullName = getText(tilFullName);
        String nickName = getText(tilNickName);

        if (!validate(fullName)) return;

        String gender       = getSelectedGender();
        String ageGroup     = getSelectedAgeGroup();
        String relationship = getSelectedRelationship();

        ChildProfile profile = new ChildProfile(fullName, nickName, selectedBirthDate,
                gender, ageGroup);

        String parentId = authService.getCurrentUser() != null
                ? authService.getCurrentUser().getUid() : null;

        if (parentId == null) {
            showToast("Lỗi: không xác định được tài khoản phụ huynh");
            return;
        }

        showLoading(progressBar);
        setFormEnabled(false);

        childProfileService.createChildProfile(parentId, profile, relationship)
                .addOnSuccessListener(childId -> {
                    hideLoading(progressBar);
                    showToast("🎉 Đã tạo hồ sơ cho bé " + profile.getDisplayName());
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    hideLoading(progressBar);
                    setFormEnabled(true);
                    showToast("Tạo hồ sơ thất bại: " + e.getMessage());
                });
    }

    // ==================== VALIDATION ====================

    private boolean validate(String fullName) {
        if (TextUtils.isEmpty(fullName) || fullName.length() < 2) {
            tilFullName.setError("Vui lòng nhập họ tên bé (tối thiểu 2 ký tự)");
            return false;
        }
        return true;
    }

    // ==================== HELPERS ====================

    private String getText(TextInputLayout til) {
        if (til.getEditText() == null) return "";
        return til.getEditText().getText().toString().trim();
    }

    private void clearErrors() {
        tilFullName.setError(null);
        tilBirthDate.setError(null);
    }

    private String getSelectedGender() {
        int id = rgGender.getCheckedRadioButtonId();
        if (id == R.id.rb_female) return "female";
        return "male";
    }

    private String getSelectedAgeGroup() {
        int id = rgAgeGroup.getCheckedRadioButtonId();
        if (id == R.id.rb_age_3_5)  return AppConstants.AGE_GROUP_3_5;
        if (id == R.id.rb_age_9_12) return AppConstants.AGE_GROUP_9_12;
        return AppConstants.AGE_GROUP_6_8; // default
    }

    private String getSelectedRelationship() {
        int id = rgRelationship.getCheckedRadioButtonId();
        if (id == R.id.rb_mother)   return "mother";
        if (id == R.id.rb_guardian) return "guardian";
        return "father";
    }

    private void setFormEnabled(boolean enabled) {
        if (tilFullName.getEditText() != null) tilFullName.getEditText().setEnabled(enabled);
        if (tilNickName.getEditText() != null) tilNickName.getEditText().setEnabled(enabled);
        rgGender.setEnabled(enabled);
        rgAgeGroup.setEnabled(enabled);
        rgRelationship.setEnabled(enabled);
        MaterialButton btn = findViewById(R.id.btn_save);
        if (btn != null) btn.setEnabled(enabled);
    }
}

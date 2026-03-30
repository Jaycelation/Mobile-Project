package com.example.kid_app.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.parent.ParentHomeActivity;
import com.example.kid_app.teacher.TeacherHomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

/**
 * SignUpActivity — màn hình đăng ký.
 *
 * Flow:
 * 1. Validate họ tên, email, mật khẩu, xác nhận mật khẩu, role.
 * 2. Gọi AuthService.signUp() — Firebase Auth + tạo document Firestore.
 * 3. Sau khi thành công, điều hướng theo role đã chọn.
 *
 * Admin KHÔNG đăng ký qua app — bỏ qua, chỉ hỗ trợ parent và teacher.
 */
public class SignUpActivity extends BaseActivity {

    private AuthService authService;

    private TextInputLayout tilFullName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private RadioGroup      rgRole;
    private ProgressBar     progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        authService = new AuthService();

        bindViews();
    }

    private void bindViews() {
        tilFullName        = findViewById(R.id.til_full_name);
        tilEmail           = findViewById(R.id.til_email);
        tilPassword        = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        rgRole             = findViewById(R.id.rg_role);
        progressBar        = findViewById(R.id.progress_bar);

        MaterialButton btnSignUp  = findViewById(R.id.btn_sign_up);
        TextView       tvGoSignIn = findViewById(R.id.tv_go_sign_in);

        btnSignUp.setOnClickListener(v -> attemptSignUp());

        tvGoSignIn.setOnClickListener(v -> {
            navigateTo(SignInActivity.class);
            finish();
        });
    }

    // ==================== SIGN UP LOGIC ====================

    private void attemptSignUp() {
        clearErrors();

        String fullName        = getText(tilFullName);
        String email           = getText(tilEmail);
        String password        = getText(tilPassword);
        String confirmPassword = getText(tilConfirmPassword);
        String role            = getSelectedRole();

        if (!validateSignUp(fullName, email, password, confirmPassword)) return;

        showLoading(progressBar);
        setFormEnabled(false);

        authService.signUp(email, password, fullName, role)
                .addOnSuccessListener(unused -> {
                    hideLoading(progressBar);
                    showToast("Đăng ký thành công! Chào mừng bạn 🎉");
                    navigateByRole(role);
                })
                .addOnFailureListener(e -> {
                    hideLoading(progressBar);
                    setFormEnabled(true);
                    showToast(mapAuthError(e.getMessage()));
                });
    }

    private String getSelectedRole() {
        int selectedId = rgRole.getCheckedRadioButtonId();
        if (selectedId == R.id.rb_teacher) return AppConstants.ROLE_TEACHER;
        return AppConstants.ROLE_PARENT; // default
    }

    private void navigateByRole(String role) {
        if (AppConstants.ROLE_TEACHER.equals(role)) {
            navigateToClearStack(TeacherHomeActivity.class);
        } else {
            navigateToClearStack(ParentHomeActivity.class);
        }
    }

    // ==================== VALIDATION ====================

    private boolean validateSignUp(String fullName, String email,
                                    String password, String confirmPassword) {
        boolean valid = true;

        if (TextUtils.isEmpty(fullName) || fullName.length() < 2) {
            tilFullName.setError("Vui lòng nhập họ tên (tối thiểu 2 ký tự)");
            valid = false;
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Vui lòng nhập email");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không đúng định dạng");
            valid = false;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            valid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Mật khẩu tối thiểu 6 ký tự");
            valid = false;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            valid = false;
        }

        return valid;
    }

    // ==================== UTILS ====================

    private String getText(TextInputLayout til) {
        if (til.getEditText() == null) return "";
        return til.getEditText().getText().toString().trim();
    }

    private void clearErrors() {
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    private void setFormEnabled(boolean enabled) {
        tilFullName.setEnabled(enabled);
        tilEmail.setEnabled(enabled);
        tilPassword.setEnabled(enabled);
        tilConfirmPassword.setEnabled(enabled);
        rgRole.setEnabled(enabled);
        MaterialButton btn = findViewById(R.id.btn_sign_up);
        if (btn != null) btn.setEnabled(enabled);
    }

    private String mapAuthError(String message) {
        if (message == null) return getString(R.string.error_generic);
        if (message.contains("email-already-in-use"))
            return "Email này đã được đăng ký";
        if (message.contains("invalid-email"))
            return "Địa chỉ email không hợp lệ";
        if (message.contains("weak-password"))
            return "Mật khẩu quá yếu, hãy chọn mật khẩu mạnh hơn";
        if (message.contains("network"))
            return getString(R.string.error_network);
        return getString(R.string.error_generic);
    }
}

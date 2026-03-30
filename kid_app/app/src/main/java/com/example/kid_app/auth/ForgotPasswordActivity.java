package com.example.kid_app.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.example.kid_app.R;
import com.example.kid_app.common.BaseActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

/**
 * ForgotPasswordActivity — màn hình quên mật khẩu.
 *
 * Flow:
 * 1. Nhập email hợp lệ.
 * 2. Gọi AuthService.sendPasswordResetEmail().
 * 3. Firebase tự gửi link reset — app hiển thị thông báo thành công.
 * 4. Sau khi gửi xong, quay lại SignInActivity.
 */
public class ForgotPasswordActivity extends BaseActivity {

    private AuthService authService;

    private TextInputLayout tilEmail;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        authService = new AuthService();

        bindViews();
    }

    private void bindViews() {
        tilEmail    = findViewById(R.id.til_email);
        progressBar = findViewById(R.id.progress_bar);

        ImageButton        btnBack      = findViewById(R.id.btn_back);
        MaterialButton     btnSendReset = findViewById(R.id.btn_send_reset);

        btnBack.setOnClickListener(v -> finish());

        btnSendReset.setOnClickListener(v -> attemptSendReset());
    }

    // ==================== LOGIC ====================

    private void attemptSendReset() {
        tilEmail.setError(null);

        String email = getText(tilEmail);

        if (!validateEmail(email)) return;

        showLoading(progressBar);
        setFormEnabled(false);

        authService.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    hideLoading(progressBar);
                    showLongToast("✉️ Email đặt lại mật khẩu đã được gửi tới " + email);
                    // Quay lại màn đăng nhập sau khi gửi thành công
                    navigateToAndFinish(SignInActivity.class);
                })
                .addOnFailureListener(e -> {
                    hideLoading(progressBar);
                    setFormEnabled(true);
                    showToast(mapResetError(e.getMessage()));
                });
    }

    // ==================== VALIDATION ====================

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Vui lòng nhập email");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không đúng định dạng");
            return false;
        }
        return true;
    }

    // ==================== UTILS ====================

    private String getText(TextInputLayout til) {
        if (til.getEditText() == null) return "";
        return til.getEditText().getText().toString().trim();
    }

    private void setFormEnabled(boolean enabled) {
        if (tilEmail.getEditText() != null) tilEmail.getEditText().setEnabled(enabled);
        MaterialButton btn = findViewById(R.id.btn_send_reset);
        if (btn != null) btn.setEnabled(enabled);
    }

    private String mapResetError(String message) {
        if (message == null) return getString(R.string.error_generic);
        if (message.contains("user-not-found") || message.contains("no user record"))
            return "Email này chưa được đăng ký trong hệ thống";
        if (message.contains("invalid-email"))
            return "Địa chỉ email không hợp lệ";
        if (message.contains("network"))
            return getString(R.string.error_network);
        return getString(R.string.error_generic);
    }
}

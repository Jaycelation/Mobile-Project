package com.example.kid_app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.kid_app.R;
import com.example.kid_app.admin.AdminHomeActivity;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.mapper.DocumentMapper;
import com.example.kid_app.data.model.Account;
import com.example.kid_app.parent.ParentHomeActivity;
import com.example.kid_app.teacher.TeacherHomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * SignInActivity — màn hình đăng nhập.
 *
 * Flow:
 * 1. Validate email + password.
 * 2. Gọi AuthService.signIn().
 * 3. Sau khi thành công, lấy document /accounts/{uid} để đọc role.
 * 4. Điều hướng theo role:
 *    - parent  → ParentHomeActivity
 *    - teacher → TeacherHomeActivity
 *    - admin   → AdminHomeActivity
 */
public class SignInActivity extends BaseActivity {

    private AuthService authService;

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        authService = new AuthService();

        // Nếu đang đăng nhập rồi thì skip
        if (authService.isLoggedIn()) {
            fetchRoleAndNavigate();
            return;
        }

        bindViews();
    }

    private void bindViews() {
        tilEmail    = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        progressBar = findViewById(R.id.progress_bar);

        MaterialButton btnSignIn      = findViewById(R.id.btn_sign_in);
        TextView       tvForgotPass   = findViewById(R.id.tv_forgot_password);
        TextView       tvGoSignUp     = findViewById(R.id.tv_go_sign_up);

        btnSignIn.setOnClickListener(v -> attemptSignIn());

        tvForgotPass.setOnClickListener(v ->
                navigateTo(ForgotPasswordActivity.class));

        tvGoSignUp.setOnClickListener(v -> {
            navigateTo(SignUpActivity.class);
            finish();
        });
    }

    // ==================== SIGN IN LOGIC ====================

    private void attemptSignIn() {
        clearErrors();

        String email    = getText(tilEmail);
        String password = getText(tilPassword);

        if (!validateSignIn(email, password)) return;

        showLoading(progressBar);
        setFormEnabled(false);

        authService.signIn(email, password)
                .addOnSuccessListener(authResult -> fetchRoleAndNavigate())
                .addOnFailureListener(e -> {
                    hideLoading(progressBar);
                    setFormEnabled(true);
                    showToast(mapAuthError(e.getMessage()));
                });
    }

    /** Sau khi đăng nhập thành công, lấy role từ Firestore rồi điều hướng */
    private void fetchRoleAndNavigate() {
        authService.getCurrentUserAccount()
                .addOnSuccessListener(doc -> {
                    hideLoading(progressBar);
                    Account account = DocumentMapper.toAccount(doc);
                    if (account == null) {
                        showToast("Không tìm thấy thông tin tài khoản");
                        authService.signOut();
                        setFormEnabled(true);
                        return;
                    }
                    navigateByRole(account.getRole());
                })
                .addOnFailureListener(e -> {
                    hideLoading(progressBar);
                    showToast("Lỗi khi tải thông tin tài khoản: " + e.getMessage());
                    setFormEnabled(true);
                });
    }

    private void navigateByRole(String role) {
        Class<?> destination;
        switch (role) {
            case AppConstants.ROLE_PARENT:
                destination = ParentHomeActivity.class;
                break;
            case AppConstants.ROLE_TEACHER:
                destination = TeacherHomeActivity.class;
                break;
            case AppConstants.ROLE_ADMIN:
                destination = AdminHomeActivity.class;
                break;
            default:
                showToast("Role không hợp lệ: " + role);
                authService.signOut();
                setFormEnabled(true);
                return;
        }
        navigateToClearStack(destination);
    }

    // ==================== VALIDATION ====================

    private boolean validateSignIn(String email, String password) {
        boolean valid = true;

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

        return valid;
    }

    // ==================== UTILS ====================

    private String getText(TextInputLayout til) {
        if (til.getEditText() == null) return "";
        return til.getEditText().getText().toString().trim();
    }

    private void clearErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
    }

    private void setFormEnabled(boolean enabled) {
        if (tilEmail.getEditText() != null)    tilEmail.getEditText().setEnabled(enabled);
        if (tilPassword.getEditText() != null) tilPassword.getEditText().setEnabled(enabled);
        MaterialButton btn = findViewById(R.id.btn_sign_in);
        if (btn != null) btn.setEnabled(enabled);
    }

    /**
     * Map lỗi Firebase Auth từ tiếng Anh sang tiếng Việt.
     * Chỉ xử lý các case phổ biến nhất.
     */
    private String mapAuthError(String message) {
        if (message == null) return getString(R.string.error_generic);
        if (message.contains("no user record") || message.contains("user-not-found"))
            return "Email chưa được đăng ký";
        if (message.contains("wrong-password") || message.contains("invalid-credential"))
            return "Mật khẩu không đúng";
        if (message.contains("too-many-requests"))
            return "Quá nhiều lần thử. Vui lòng thử lại sau.";
        if (message.contains("network"))
            return getString(R.string.error_network);
        return getString(R.string.error_generic);
    }
}

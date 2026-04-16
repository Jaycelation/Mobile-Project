package com.example.kid_app.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.parent.ParentHomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

public class SignUpActivity extends BaseActivity {

    private TextInputLayout tilFullName, tilEmail, tilPassword, tilConfirmPassword;
    private ProgressBar progressBar;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        authService = new AuthService();
        bindViews();
    }

    private void bindViews() {
        tilFullName = findViewById(R.id.til_full_name);
        tilEmail    = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        progressBar = findViewById(R.id.progress_bar);

        MaterialButton btnSignUp = findViewById(R.id.btn_sign_up);
        TextView tvGoSignIn = findViewById(R.id.tv_go_sign_in);

        btnSignUp.setOnClickListener(v -> attemptSignUp());
        tvGoSignIn.setOnClickListener(v -> {
            navigateTo(SignInActivity.class);
            finish();
        });
    }

    private void attemptSignUp() {
        String name = tilFullName.getEditText().getText().toString().trim();
        String email = tilEmail.getEditText().getText().toString().trim();
        String pass = tilPassword.getEditText().getText().toString().trim();
        String confirmPass = tilConfirmPassword.getEditText().getText().toString().trim();

        if (TextUtils.isEmpty(name)) { 
            tilFullName.setError("Vui lòng nhập họ tên"); 
            return; 
        } else {
            tilFullName.setError(null);
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { 
            tilEmail.setError("Email không hợp lệ"); 
            return; 
        } else {
            tilEmail.setError(null);
        }

        if (pass.length() < 6) { 
            tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự"); 
            return; 
        } else {
            tilPassword.setError(null);
        }

        if (!pass.equals(confirmPass)) {
            tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        } else {
            tilConfirmPassword.setError(null);
        }

        showLoading(progressBar);
        
        // MẶC ĐỊNH: Đăng ký với vai trò PARENT (Phụ huynh)
        authService.signUp(email, pass, name, AppConstants.ROLE_PARENT)
                .addOnSuccessListener(aVoid -> {
                    hideLoading(progressBar);
                    showToast("Đăng ký tài khoản phụ huynh thành công!");
                    navigateToClearStack(ParentHomeActivity.class);
                })
                .addOnFailureListener(e -> {
                    hideLoading(progressBar);
                    showToast("Lỗi đăng ký: " + e.getMessage());
                });
    }
}

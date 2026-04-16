package com.example.kid_app.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.kid_app.R;
import com.example.kid_app.admin.AdminHomeActivity;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.parent.ParentHomeActivity;
import com.example.kid_app.teacher.TeacherHomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends BaseActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        bindViews();

        if (auth.getCurrentUser() != null) {
            loadUserAndNavigate();
        }
    }

    private void bindViews() {
        tilEmail    = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        progressBar = findViewById(R.id.progress_bar);

        MaterialButton btnSignIn = findViewById(R.id.btn_sign_in);
        TextView tvGoSignUp = findViewById(R.id.tv_go_sign_up);

        btnSignIn.setOnClickListener(v -> attemptSignIn());

        tvGoSignUp.setOnClickListener(v -> {
            navigateTo(SignUpActivity.class);
            finish();
        });
    }

    private void attemptSignIn() {
        clearErrors();

        String email    = getText(tilEmail);
        String password = getText(tilPassword);

        if (!validate(email, password)) return;

        showLoading(progressBar);
        setFormEnabled(false);

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> loadUserAndNavigate())
                .addOnFailureListener(e -> {
                    hideLoading(progressBar);
                    setFormEnabled(true);
                    showToast("Sai tài khoản hoặc mật khẩu");
                });
    }

    private void loadUserAndNavigate() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("accounts")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    hideLoading(progressBar);

                    if (!doc.exists()) {
                        showToast("Chưa có dữ liệu người dùng trên hệ thống");
                        return;
                    }

                    String role = doc.getString("role");

                    if (AppConstants.ROLE_ADMIN.equals(role)) {
                        navigateToClearStack(AdminHomeActivity.class);
                    } else if (AppConstants.ROLE_TEACHER.equals(role)) {
                        navigateToClearStack(TeacherHomeActivity.class);
                    } else {
                        navigateToClearStack(ParentHomeActivity.class);
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoading(progressBar);
                    showToast("Lỗi kết nối dữ liệu: " + e.getMessage());
                });
    }

    private boolean validate(String email, String password) {
        boolean valid = true;

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Nhập email");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ");
            valid = false;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Nhập mật khẩu");
            valid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Mật khẩu tối thiểu 6 ký tự");
            valid = false;
        }

        return valid;
    }

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
}

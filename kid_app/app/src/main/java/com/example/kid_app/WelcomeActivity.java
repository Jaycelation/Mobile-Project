package com.example.kid_app;

import android.os.Bundle;

import com.example.kid_app.auth.SignInActivity;
import com.example.kid_app.auth.SignUpActivity;
import com.example.kid_app.common.BaseActivity;
import com.google.android.material.button.MaterialButton;

/**
 * WelcomeActivity — màn hình chào mừng.
 *
 * Hiển thị logo app + 2 nút:
 * - Đăng nhập → SignInActivity
 * - Đăng ký  → SignUpActivity
 *
 * Không cần kiểm tra auth ở đây.
 * SplashActivity đã xử lý việc bỏ qua WelcomeActivity nếu đã đăng nhập.
 */
public class WelcomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        MaterialButton btnSignIn = findViewById(R.id.btn_sign_in);
        MaterialButton btnSignUp = findViewById(R.id.btn_sign_up);

        btnSignIn.setOnClickListener(v -> navigateTo(SignInActivity.class));
        btnSignUp.setOnClickListener(v -> navigateTo(SignUpActivity.class));
    }
}


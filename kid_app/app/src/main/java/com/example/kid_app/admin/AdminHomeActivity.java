package com.example.kid_app.admin;

import android.os.Bundle;
import android.widget.TextView;

import com.example.kid_app.R;
import com.example.kid_app.auth.AuthService;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.mapper.DocumentMapper;
import com.example.kid_app.data.model.Account;
import com.google.android.material.button.MaterialButton;

/**
 * AdminHomeActivity — màn hình chính của Admin.
 *
 * Hiện tại là placeholder, sẽ được hoàn thiện ở Bước 13 với:
 * - Xem danh sách người dùng
 * - CRUD content_catalog
 * - Xem báo cáo tổng hợp
 *
 * Lưu ý quan trọng:
 * - Admin KHÔNG đăng ký qua app — tài khoản được tạo thủ công
 *   (ví dụ: qua Firebase Console hoặc Cloud Functions).
 * - Khi đăng nhập bằng email admin, role="admin" được lưu trong
 *   Firestore và SplashActivity/SignInActivity sẽ điều hướng tới đây.
 */
public class AdminHomeActivity extends BaseActivity {

    private AuthService authService;
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        authService = new AuthService();

        bindViews();
        loadUserInfo();
    }

    private void bindViews() {
        tvWelcome = findViewById(R.id.tv_welcome);

        MaterialButton btnSignOut = findViewById(R.id.btn_sign_out);
        btnSignOut.setOnClickListener(v -> signOut());
    }

    /**
     * Tải thông tin tài khoản admin từ Firestore.
     */
    private void loadUserInfo() {
        authService.getCurrentUserAccount()
                .addOnSuccessListener(doc -> {
                    Account account = DocumentMapper.toAccount(doc);
                    if (account != null && account.getFullName() != null) {
                        tvWelcome.setText("Xin chào, " + account.getFullName() + " 🛡️");
                    }
                })
                .addOnFailureListener(e -> {
                    // Không block UI, chỉ giữ text mặc định
                });
    }

    private void signOut() {
        authService.signOut();
        navigateToClearStack(com.example.kid_app.WelcomeActivity.class);
    }
}

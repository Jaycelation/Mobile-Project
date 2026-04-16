package com.example.kid_app.admin;

import android.os.Bundle;
import android.widget.TextView;

import com.example.kid_app.R;
import com.example.kid_app.auth.AuthService;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.mapper.DocumentMapper;
import com.example.kid_app.data.model.Account;

/**
 * AdminHomeActivity — màn hình chính của Admin.
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

        // Chức năng 1: Quản lý người dùng
        findViewById(R.id.card_manage_users).setOnClickListener(v -> {
            navigateTo(UserManagementActivity.class);
        });

        // Chức năng 2: Quản lý nội dung học
        findViewById(R.id.card_manage_content).setOnClickListener(v -> {
            navigateTo(ContentManagementActivity.class);
        });

        // Chức năng 3: Xem báo cáo hệ thống
        findViewById(R.id.card_reports).setOnClickListener(v -> {
            navigateTo(SystemReportActivity.class);
        });

        findViewById(R.id.card_sign_out).setOnClickListener(v -> signOut());
    }

    private void loadUserInfo() {
        authService.getCurrentUserAccount()
                .addOnSuccessListener(doc -> {
                    Account account = DocumentMapper.toAccount(doc);
                    if (account != null && account.getFullName() != null) {
                        tvWelcome.setText("Xin chào, " + account.getFullName() + " 🛡️");
                    }
                })
                .addOnFailureListener(e -> {
                    // Mặc định là "Admin" nếu lỗi
                });
    }

    private void signOut() {
        authService.signOut();
        navigateToClearStack(com.example.kid_app.WelcomeActivity.class);
    }
}

package com.example.kid_app.teacher;

import android.os.Bundle;
import android.widget.TextView;

import com.example.kid_app.R;
import com.example.kid_app.auth.AuthService;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.mapper.DocumentMapper;
import com.example.kid_app.data.model.Account;
import com.google.android.material.button.MaterialButton;

/**
 * TeacherHomeActivity — màn hình chính của Giáo viên.
 *
 * Hiện tại là placeholder, sẽ được hoàn thiện ở Bước 11 với:
 * - Danh sách lớp học
 * - Tạo và giao bài tập
 * - Theo dõi assignment_submissions
 *
 * Luồng hiện tại:
 * 1. Load tên giáo viên từ Firestore.
 * 2. Hiển thị welcome message.
 * 3. Nút Đăng xuất → WelcomeActivity.
 */
public class TeacherHomeActivity extends BaseActivity {

    private AuthService authService;
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_home);

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
     * Tải thông tin tài khoản từ Firestore để hiển thị tên giáo viên.
     */
    private void loadUserInfo() {
        authService.getCurrentUserAccount()
                .addOnSuccessListener(doc -> {
                    Account account = DocumentMapper.toAccount(doc);
                    if (account != null && account.getFullName() != null) {
                        tvWelcome.setText("Xin chào, thầy/cô " + account.getFullName() + "! 👩‍🏫");
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

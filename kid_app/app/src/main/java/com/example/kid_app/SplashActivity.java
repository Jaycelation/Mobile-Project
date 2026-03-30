package com.example.kid_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kid_app.admin.AdminHomeActivity;
import com.example.kid_app.auth.AuthService;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.data.mapper.DocumentMapper;
import com.example.kid_app.data.model.Account;
import com.example.kid_app.parent.ParentHomeActivity;
import com.example.kid_app.teacher.TeacherHomeActivity;
import com.google.firebase.auth.FirebaseUser;

/**
 * SplashActivity — màn hình khởi động.
 *
 * Hiển thị splash 1.5s, sau đó:
 * - Nếu chưa đăng nhập → WelcomeActivity
 * - Nếu đã đăng nhập → lấy role từ Firestore → điều hướng đúng màn hình
 *
 * Lưu ý về Child:
 * - Child không đăng nhập qua Firebase Auth.
 * - Child được chọn qua ParentHomeActivity sau khi Parent đăng nhập.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION_MS = 1500;

    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        authService = new AuthService();

        new Handler(Looper.getMainLooper()).postDelayed(
                this::checkAuthAndNavigate,
                SPLASH_DURATION_MS
        );
    }

    /**
     * Kiểm tra trạng thái đăng nhập và điều hướng phù hợp.
     *
     * Nếu đã đăng nhập: lấy account document từ Firestore để đọc role.
     * Nếu Firestore lỗi (offline, timeout): đăng xuất và về Welcome để an toàn.
     */
    private void checkAuthAndNavigate() {
        FirebaseUser currentUser = authService.getCurrentUser();

        if (currentUser == null) {
            // Chưa đăng nhập → về màn Welcome
            navigateTo(WelcomeActivity.class);
            return;
        }

        // Đã đăng nhập → lấy role từ Firestore
        authService.getCurrentUserAccount()
                .addOnSuccessListener(doc -> {
                    Account account = DocumentMapper.toAccount(doc);
                    if (account == null || account.getRole() == null) {
                        // Document không hợp lệ → đăng xuất an toàn
                        authService.signOut();
                        navigateTo(WelcomeActivity.class);
                        return;
                    }
                    navigateByRole(account.getRole());
                })
                .addOnFailureListener(e -> {
                    // Không lấy được account (lỗi mạng, v.v.)
                    // Đăng xuất và về Welcome thay vì để app treo
                    authService.signOut();
                    navigateTo(WelcomeActivity.class);
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
                // Role không xác định → về Welcome
                authService.signOut();
                destination = WelcomeActivity.class;
        }
        navigateTo(destination);
    }

    private void navigateTo(Class<?> destination) {
        Intent intent = new Intent(this, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

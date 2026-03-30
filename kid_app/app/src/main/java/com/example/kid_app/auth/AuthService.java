package com.example.kid_app.auth;

import com.example.kid_app.common.AppConstants;
import com.example.kid_app.data.FirestoreHelper;
import com.example.kid_app.data.model.Account;
import com.example.kid_app.data.repository.AccountRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * AuthService — lớp trung gian giữa FirebaseAuth và UI.
 *
 * Không được gọi Firebase trực tiếp từ Activity/Fragment.
 * Mọi logic auth phải qua class này.
 *
 * Thiết kế:
 * - Trả về Task<> để UI tự gắn listener (không callback hell).
 * - Không giữ context Android, không leak.
 * - Tách rõ: xác thực (Auth) vs lưu dữ liệu (Repository).
 *
 * Lưu ý về Child:
 * - Child KHÔNG đăng nhập trực tiếp bằng Firebase Auth.
 * - Child được truy cập qua profile do Parent tạo và chọn.
 * - Khi Parent đăng nhập thành công, họ chọn "đang học với bé nào"
 *   và lưu childId vào SharedPreferences.
 */
public class AuthService {

    private final FirebaseAuth firebaseAuth;
    private final AccountRepository accountRepository;

    public AuthService() {
        this.firebaseAuth = FirestoreHelper.getInstance().getAuth();
        this.accountRepository = new AccountRepository();
    }

    // ==================== TRẠNG THÁI HIỆN TẠI ====================

    /** Lấy user đang đăng nhập. Null nếu chưa đăng nhập. */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    /** Kiểm tra đã đăng nhập chưa */
    public boolean isLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    // ==================== ĐĂNG NHẬP ====================

    /**
     * Đăng nhập bằng email/password.
     * UI cần gọi getAccount() sau khi đăng nhập để biết role.
     */
    public Task<AuthResult> signIn(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email.trim(), password);
    }

    // ==================== ĐĂNG KÝ ====================

    /**
     * Đăng ký tài khoản mới và tạo document trong Firestore.
     *
     * Flow:
     * 1. Tạo user trong Firebase Auth.
     * 2. Tạo document /accounts/{uid} trong Firestore.
     *
     * Role được truyền vào ("parent" | "teacher").
     * Không hỗ trợ role "admin" đăng ký qua app — admin được tạo thủ công.
     */
    public Task<Void> signUp(String email, String password, String fullName, String role) {
        return firebaseAuth
                .createUserWithEmailAndPassword(email.trim(), password)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException() != null
                                ? task.getException()
                                : new Exception("Đăng ký thất bại");
                    }
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user == null) throw new Exception("Không thể lấy user sau đăng ký");

                    Account account = new Account(
                            user.getUid(),
                            email.trim(),
                            fullName.trim(),
                            role
                    );
                    return accountRepository.createAccount(user.getUid(), account);
                });
    }

    // ==================== QUÊN MẬT KHẨU ====================

    /**
     * Gửi email đặt lại mật khẩu.
     * Firebase Auth tự xử lý — app không lưu gì thêm.
     */
    public Task<Void> sendPasswordResetEmail(String email) {
        return firebaseAuth.sendPasswordResetEmail(email.trim());
    }

    // ==================== LẤY ROLE ====================

    /**
     * Lấy account document của user hiện tại từ Firestore.
     * Dùng sau khi đăng nhập để xác định role và điều hướng.
     */
    public Task<DocumentSnapshot> getCurrentUserAccount() {
        FirebaseUser user = getCurrentUser();
        if (user == null) return Tasks.forException(new Exception("Chưa đăng nhập"));
        return accountRepository.getAccount(user.getUid());
    }

    // ==================== ĐĂNG XUẤT ====================

    /** Đăng xuất — xóa session Firebase Auth */
    public void signOut() {
        firebaseAuth.signOut();
    }
}

package com.example.kid_app.data.repository;

import com.example.kid_app.common.AppConstants;
import com.example.kid_app.data.FirestoreHelper;
import com.example.kid_app.data.model.Account;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * AccountRepository — thao tác với /accounts collection.
 * Được dùng sau khi FirebaseAuth tạo user thành công.
 */
public class AccountRepository {

    private final FirebaseFirestore db;

    public AccountRepository() {
        this.db = FirestoreHelper.getInstance().getFirestore();
    }

    /** Tạo document account mới (sau khi đăng ký thành công) */
    public Task<Void> createAccount(String uid, Account account) {
        return db.collection(AppConstants.COL_ACCOUNTS)
                .document(uid)
                .set(account);
    }

    /** Lấy account theo uid */
    public Task<DocumentSnapshot> getAccount(String uid) {
        return db.collection(AppConstants.COL_ACCOUNTS)
                .document(uid)
                .get();
    }

    /** Cập nhật thông tin account */
    public Task<Void> updateAccount(String uid, Account account) {
        return db.collection(AppConstants.COL_ACCOUNTS)
                .document(uid)
                .set(account);
    }

    /** Soft delete — ghi deleted_at */
    public Task<Void> softDeleteAccount(String uid) {
        return db.collection(AppConstants.COL_ACCOUNTS)
                .document(uid)
                .update("deletedAt", new java.util.Date(),
                        "status", AppConstants.STATUS_DELETED);
    }

    /** Lấy tất cả tài khoản (chỉ Admin dùng) */
    public Task<QuerySnapshot> getAllAccounts() {
        return db.collection(AppConstants.COL_ACCOUNTS)
                .whereEqualTo("deletedAt", null)
                .get();
    }

    /** Lấy tài khoản theo role */
    public Task<QuerySnapshot> getAccountsByRole(String role) {
        return db.collection(AppConstants.COL_ACCOUNTS)
                .whereEqualTo("role", role)
                .whereEqualTo("deletedAt", null)
                .get();
    }
}

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
 */
public class AccountRepository {

    private final FirebaseFirestore db;

    public AccountRepository() {
        this.db = FirestoreHelper.getInstance().getFirestore();
    }

    /** Tạo document account mới */
    // Chuc nang: tao moi tai khoan nguoi dung trong collection accounts.
    public Task<Void> createAccount(String uid, Account account) {
        return db.collection(AppConstants.COL_ACCOUNTS)
                .document(uid)
                .set(account);
    }

    /** Lấy account theo uid */
    // Chuc nang: doc thong tin tai khoan theo uid de lay ho ten va vai tro.
    public Task<DocumentSnapshot> getAccount(String uid) {
        return db.collection(AppConstants.COL_ACCOUNTS)
                .document(uid)
                .get();
    }

    /** Cập nhật thông tin account */
    // Chuc nang: cap nhat thong tin tai khoan tren Firestore.
    public Task<Void> updateAccount(String uid, Account account) {
        return db.collection(AppConstants.COL_ACCOUNTS)
                .document(uid)
                .set(account);
    }

    /** Xóa vĩnh viễn tài khoản khỏi Firestore */
    // Chuc nang: xoa tai khoan khoi collection accounts.
    public Task<Void> deleteAccount(String uid) {
        return db.collection(AppConstants.COL_ACCOUNTS)
                .document(uid)
                .delete();
    }

    /** Lấy tất cả tài khoản để Admin quản lý */
    // Chuc nang: lay toan bo tai khoan de quan tri vien quan ly nguoi dung.
    public Task<QuerySnapshot> getAllAccounts() {
        return db.collection(AppConstants.COL_ACCOUNTS).get();
    }

    /** Lấy tài khoản theo role */
    // Chuc nang: loc danh sach tai khoan theo vai tro parent, teacher hoac admin.
    public Task<QuerySnapshot> getAccountsByRole(String role) {
        return db.collection(AppConstants.COL_ACCOUNTS)
                .whereEqualTo("role", role)
                .get();
    }
}

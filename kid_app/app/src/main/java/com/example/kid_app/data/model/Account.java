package com.example.kid_app.data.model;

import com.example.kid_app.common.AppConstants;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model: accounts (top-level collection)
 * Firestore path: /accounts/{account_id}
 *
 * Đại diện cho tài khoản của Parent, Teacher, Admin.
 * Child KHÔNG có account riêng — truy cập qua Parent.
 */
public class Account {

    @DocumentId
    private String accountId;

    private String email;
    private String fullName;

    /** Giá trị: "parent" | "teacher" | "admin" — xem AppConstants.ROLE_* */
    private String role;

    private String avatarUrl;

    /** Giá trị: "active" | "inactive" | "deleted" */
    private String status;

    @ServerTimestamp
    private Date createdAt;

    @ServerTimestamp
    private Date updatedAt;

    /** Nullable — null nghĩa là chưa xóa (soft delete) */
    private Date deletedAt;

    // Required by Firestore
    public Account() {}

    /** Constructor dùng khi đăng ký — uid từ FirebaseAuth */
    public Account(String uid, String email, String fullName, String role) {
        this.accountId = uid;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.status = AppConstants.STATUS_ACTIVE;
    }

    /** Constructor tổng quát (không cần uid trước) */
    public Account(String email, String fullName, String role) {
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.status = AppConstants.STATUS_ACTIVE;
    }

    // Getters & Setters
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Date getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Date deletedAt) { this.deletedAt = deletedAt; }

    public boolean isDeleted() { return deletedAt != null; }
}

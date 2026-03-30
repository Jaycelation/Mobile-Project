package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model: child_profiles (top-level collection)
 * Firestore path: /child_profiles/{child_id}
 *
 * Hồ sơ trẻ em — được tạo bởi Parent.
 * Child KHÔNG tự đăng nhập. Mọi truy cập đều thông qua Parent account.
 *
 * Subcollections liên quan (tạo khi cần):
 *   /child_profiles/{child_id}/settings/child_settings  → ChildSettings
 *   /child_profiles/{child_id}/activity_attempts/       → ActivityAttempt
 */
public class ChildProfile {

    @DocumentId
    private String childId;

    private String fullName;
    private String nickName;

    /** Format: "yyyy-MM-dd" */
    private String birthDate;

    /** "male" | "female" | "other" */
    private String gender;

    private String avatarUrl;

    /** "3-5" | "6-8" | "9-12" — xem AppConstants.AGE_GROUP_* */
    private String ageGroup;

    /**
     * Nullable — mã lớp học hiện tại.
     * Đây chỉ là dữ liệu hỗ trợ để load nhanh.
     * Logic thành viên lớp dựa trên class_members collection.
     */
    private String currentClassId;

    @ServerTimestamp
    private Date createdAt;

    @ServerTimestamp
    private Date updatedAt;

    /** Nullable — soft delete */
    private Date deletedAt;

    // Required by Firestore
    public ChildProfile() {}

    public ChildProfile(String fullName, String nickName, String birthDate,
                        String gender, String ageGroup) {
        this.fullName = fullName;
        this.nickName = nickName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.ageGroup = ageGroup;
    }

    // Getters & Setters
    public String getChildId() { return childId; }
    public void setChildId(String childId) { this.childId = childId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }

    public String getCurrentClassId() { return currentClassId; }
    public void setCurrentClassId(String currentClassId) { this.currentClassId = currentClassId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Date getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Date deletedAt) { this.deletedAt = deletedAt; }

    public boolean isDeleted() { return deletedAt != null; }

    /** Lấy tên hiển thị: ưu tiên nickName, fallback về fullName */
    public String getDisplayName() {
        return (nickName != null && !nickName.isEmpty()) ? nickName : fullName;
    }
}

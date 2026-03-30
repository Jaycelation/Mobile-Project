package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;

/**
 * Model: badges (top-level collection)
 * Firestore path: /badges/{badge_id}
 *
 * Định nghĩa huy hiệu toàn hệ thống — do Admin tạo.
 * Top-level vì được chia sẻ cho mọi child.
 */
public class Badge {

    @DocumentId
    private String badgeId;

    private String badgeName;
    private String description;
    private String iconUrl;

    /**
     * Loại tiêu chí để nhận huy hiệu.
     * Ví dụ: "total_points" | "streak_days" | "total_completed" | "manual"
     */
    private String criteriaType;

    /** Giá trị ngưỡng để đạt huy hiệu (dùng với criteriaType tự động) */
    private int criteriaValue;

    /** Badge còn hiệu lực hay đã ẩn */
    private boolean isActive = true;

    // Required by Firestore
    public Badge() {}

    public Badge(String badgeName, String description, String criteriaType, int criteriaValue) {
        this.badgeName = badgeName;
        this.description = description;
        this.criteriaType = criteriaType;
        this.criteriaValue = criteriaValue;
    }

    // Getters & Setters
    public String getBadgeId() { return badgeId; }
    public void setBadgeId(String badgeId) { this.badgeId = badgeId; }

    public String getBadgeName() { return badgeName; }
    public void setBadgeName(String badgeName) { this.badgeName = badgeName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public String getCriteriaType() { return criteriaType; }
    public void setCriteriaType(String criteriaType) { this.criteriaType = criteriaType; }

    public int getCriteriaValue() { return criteriaValue; }
    public void setCriteriaValue(int criteriaValue) { this.criteriaValue = criteriaValue; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isManual() { return "manual".equals(criteriaType); }
}

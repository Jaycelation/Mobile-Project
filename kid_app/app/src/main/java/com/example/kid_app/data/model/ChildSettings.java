package com.example.kid_app.data.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model: child_settings (subcollection)
 * Firestore path: /child_profiles/{child_id}/settings/child_settings
 *
 * Lý do dùng subcollection thay vì embed vào ChildProfile:
 * - Parent thường xuyên đọc/ghi settings riêng
 * - Tách để dễ phân quyền Firestore rules sau này
 */
public class ChildSettings {

    /** Giới hạn thời gian dùng app mỗi ngày (phút) */
    private int dailyLimitMinutes;

    /** Bật/tắt tính năng AI trợ lý */
    private boolean aiEnabled;

    /**
     * Bộ lọc nội dung theo độ tuổi.
     * Giá trị: "3-5" | "6-8" | "9-12" — xem AppConstants.AGE_GROUP_*
     */
    private String contentAgeFilter;

    @ServerTimestamp
    private Date updatedAt;

    // Required by Firestore
    public ChildSettings() {}

    public ChildSettings(int dailyLimitMinutes, boolean aiEnabled, String contentAgeFilter) {
        this.dailyLimitMinutes = dailyLimitMinutes;
        this.aiEnabled = aiEnabled;
        this.contentAgeFilter = contentAgeFilter;
    }

    // Getters & Setters
    public int getDailyLimitMinutes() { return dailyLimitMinutes; }
    public void setDailyLimitMinutes(int dailyLimitMinutes) { this.dailyLimitMinutes = dailyLimitMinutes; }

    public boolean isAiEnabled() { return aiEnabled; }
    public void setAiEnabled(boolean aiEnabled) { this.aiEnabled = aiEnabled; }

    public String getContentAgeFilter() { return contentAgeFilter; }
    public void setContentAgeFilter(String contentAgeFilter) { this.contentAgeFilter = contentAgeFilter; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}

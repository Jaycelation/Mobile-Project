package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;

/**
 * Model: counting_activities (subcollection)
 * Firestore path: /content_catalog/{content_id}/detail/counting_detail
 *
 * Quan hệ 1-1 với ContentCatalog (content_type = "counting_activity").
 * Thiết kế mở rộng: hỗ trợ đếm đơn giản và phép cộng/trừ sau này.
 */
public class CountingActivity {

    @DocumentId
    private String contentId;

    /**
     * Loại hoạt động đếm.
     * Ví dụ: "count_objects" (đếm vật thể)
     *        | "simple_addition" (cộng đơn giản — mở rộng tương lai)
     *        | "simple_subtraction" (trừ đơn giản — mở rộng tương lai)
     */
    private String activityType;

    /** Số lượng tối đa cần đếm */
    private int maxCount;

    /** Bật/tắt phản hồi âm thanh khi chạm vào số */
    private boolean audioEnabled;

    /**
     * Loại vật thể hiển thị (JSON string).
     * Ví dụ: {"object_type": "apple", "emoji": "🍎"}
     * Cho phép dễ dàng mở rộng sang nhiều loại vật thể.
     */
    private String objectConfig;

    // Required by Firestore
    public CountingActivity() {}

    public CountingActivity(String contentId, String activityType, int maxCount, boolean audioEnabled) {
        this.contentId = contentId;
        this.activityType = activityType;
        this.maxCount = maxCount;
        this.audioEnabled = audioEnabled;
    }

    // Getters & Setters
    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public int getMaxCount() { return maxCount; }
    public void setMaxCount(int maxCount) { this.maxCount = maxCount; }

    public boolean isAudioEnabled() { return audioEnabled; }
    public void setAudioEnabled(boolean audioEnabled) { this.audioEnabled = audioEnabled; }

    public String getObjectConfig() { return objectConfig; }
    public void setObjectConfig(String objectConfig) { this.objectConfig = objectConfig; }
}

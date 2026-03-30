package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;

/**
 * Model: content_levels (subcollection)
 * Firestore path: /content_catalog/{content_id}/levels/{level_id}
 *
 * Subcollection vì level luôn gắn với một content cụ thể.
 * Không cần query cross-content toàn cục.
 */
public class ContentLevel {

    @DocumentId
    private String levelId;

    private String contentId;  // redundant nhưng tiện để biết cha

    /** Số thứ tự level (1, 2, 3...) */
    private int levelNo;

    /**
     * Điều kiện mở khóa level này.
     * Ví dụ: "level_1_passed" | null (nghĩa là mở sẵn)
     */
    private String unlockCondition;

    /** Điểm thưởng khi hoàn thành level này */
    private int rewardPoints;

    /**
     * Dữ liệu cấu hình dạng JSON string.
     * Ví dụ game: {"speed": 2, "obstacles": 5}
     * Ví dụ quiz: {"question_count": 10, "time_per_question": 30}
     * Ví dụ counting: {"max_count": 10, "objects": "apples"}
     */
    private String configData;

    // Required by Firestore
    public ContentLevel() {}

    public ContentLevel(String contentId, int levelNo, int rewardPoints) {
        this.contentId = contentId;
        this.levelNo = levelNo;
        this.rewardPoints = rewardPoints;
    }

    // Getters & Setters
    public String getLevelId() { return levelId; }
    public void setLevelId(String levelId) { this.levelId = levelId; }

    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }

    public int getLevelNo() { return levelNo; }
    public void setLevelNo(int levelNo) { this.levelNo = levelNo; }

    public String getUnlockCondition() { return unlockCondition; }
    public void setUnlockCondition(String unlockCondition) { this.unlockCondition = unlockCondition; }

    public int getRewardPoints() { return rewardPoints; }
    public void setRewardPoints(int rewardPoints) { this.rewardPoints = rewardPoints; }

    public String getConfigData() { return configData; }
    public void setConfigData(String configData) { this.configData = configData; }
}

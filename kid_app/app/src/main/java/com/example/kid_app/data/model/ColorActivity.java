package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;

/**
 * Model: color_activities (subcollection)
 * Firestore path: /content_catalog/{content_id}/detail/color_detail
 *
 * Quan hệ 1-1 với ContentCatalog (content_type = "color_activity").
 */
public class ColorActivity {

    @DocumentId
    private String contentId;

    /**
     * Loại hoạt động màu sắc.
     * Ví dụ: "drag_match" (kéo thả nhận biết màu)
     *        | "color_mix" (pha màu)
     *        | "fill_color" (tô màu đúng vào vung)
     */
    private String activityType;

    /**
     * Danh sách màu dùng trong hoạt động (JSON string).
     * Ví dụ: ["#FF0000", "#00FF00", "#0000FF"]
     * hoặc tên màu: ["đỏ", "xanh lá", "xanh dương"]
     */
    private String palette;

    /**
     * Quy tắc mục tiêu (JSON string).
     * Ví dụ drag_match: {"target": "red", "distractor": ["blue","green"]}
     * Ví dụ fill_color: {"zones": [{"id": "sky", "correct_color": "blue"}]}
     */
    private String targetRule;

    // Required by Firestore
    public ColorActivity() {}

    public ColorActivity(String contentId, String activityType, String palette, String targetRule) {
        this.contentId = contentId;
        this.activityType = activityType;
        this.palette = palette;
        this.targetRule = targetRule;
    }

    // Getters & Setters
    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getPalette() { return palette; }
    public void setPalette(String palette) { this.palette = palette; }

    public String getTargetRule() { return targetRule; }
    public void setTargetRule(String targetRule) { this.targetRule = targetRule; }
}

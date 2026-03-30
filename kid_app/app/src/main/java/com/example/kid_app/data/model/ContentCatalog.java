package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model: content_catalog (top-level collection)
 * Firestore path: /content_catalog/{content_id}
 *
 * Đây là "bảng chính" cho mọi loại nội dung học.
 * Mỗi content có một document chuyên biệt trong subcollection tương ứng:
 *   - game         → /content_catalog/{id}/detail/game_detail
 *   - quiz         → /content_catalog/{id}/detail/quiz_detail
 *   - color_activity → /content_catalog/{id}/detail/color_detail
 *   - counting_activity → /content_catalog/{id}/detail/counting_detail
 *
 * Subcollections:
 *   - /content_catalog/{id}/levels/ → ContentLevel
 *   - /content_catalog/{id}/questions/ → QuizQuestion (chỉ với quiz)
 */
public class ContentCatalog {

    @DocumentId
    private String contentId;

    /** "game" | "quiz" | "color_activity" | "counting_activity" */
    private String contentType;

    private String title;
    private String description;

    /** "3-5" | "6-8" | "9-12" */
    private String ageGroup;

    /** "easy" | "medium" | "hard" */
    private String difficulty;

    private String thumbnailUrl;

    /** "active" | "inactive" | "deleted" */
    private String status;

    private String createdBy;  // account_id của admin/teacher tạo

    @ServerTimestamp
    private Date createdAt;

    @ServerTimestamp
    private Date updatedAt;

    /** Nullable — soft delete */
    private Date deletedAt;

    // Required by Firestore
    public ContentCatalog() {}

    public ContentCatalog(String contentType, String title, String ageGroup, String difficulty) {
        this.contentType = contentType;
        this.title = title;
        this.ageGroup = ageGroup;
        this.difficulty = difficulty;
        this.status = "active";
    }

    // Getters & Setters
    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Date getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Date deletedAt) { this.deletedAt = deletedAt; }

    public boolean isDeleted() { return deletedAt != null; }
    public boolean isActive() { return "active".equals(status); }
}

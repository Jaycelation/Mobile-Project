package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model: child_badges (top-level collection)
 * Firestore path: /child_badges/{child_badge_id}
 *
 * Top-level thay vì subcollection vì cần query:
 * - "Tất cả badge của child X"
 * - "Ai đã nhận badge Y" (admin reporting)
 */
public class ChildBadge {

    @DocumentId
    private String childBadgeId;

    private String childId;
    private String badgeId;

    /** "system" (tự động) | "teacher" (giáo viên trao) */
    private String awardedBy;

    /** "auto_points" | "auto_streak" | "teacher_manual" | "activity_complete" */
    private String sourceType;

    /** Nullable — ID của attempt hoặc assignment liên quan */
    private String sourceId;

    @ServerTimestamp
    private Date awardedAt;

    // Required by Firestore
    public ChildBadge() {}

    public ChildBadge(String childId, String badgeId, String awardedBy,
                      String sourceType, String sourceId) {
        this.childId = childId;
        this.badgeId = badgeId;
        this.awardedBy = awardedBy;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
    }

    // Getters & Setters
    public String getChildBadgeId() { return childBadgeId; }
    public void setChildBadgeId(String childBadgeId) { this.childBadgeId = childBadgeId; }

    public String getChildId() { return childId; }
    public void setChildId(String childId) { this.childId = childId; }

    public String getBadgeId() { return badgeId; }
    public void setBadgeId(String badgeId) { this.badgeId = badgeId; }

    public String getAwardedBy() { return awardedBy; }
    public void setAwardedBy(String awardedBy) { this.awardedBy = awardedBy; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public Date getAwardedAt() { return awardedAt; }
    public void setAwardedAt(Date awardedAt) { this.awardedAt = awardedAt; }
}

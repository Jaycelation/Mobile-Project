package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model: activity_attempts (subcollection)
 * Firestore path: /child_profiles/{child_id}/activity_attempts/{attempt_id}
 *
 * Lý do subcollection thay vì top-level:
 * - Query phổ biến nhất là "tất cả attempt của child X" → subcollection tự nhiên hơn
 * - Tránh collection lớn với nhiều document của nhiều child khác nhau
 *
 * Tuy nhiên: nếu cần aggregate toàn hệ thống (admin report), có thể dùng
 * Collection Group Query: collectionGroup("activity_attempts")
 */
public class ActivityAttempt {

    @DocumentId
    private String attemptId;

    private String childId;
    private String contentId;
    private String levelId;

    /** Nullable — chỉ có khi làm bài theo assignment của giáo viên */
    private String assignmentId;

    /** "game" | "quiz" | "color" | "counting" */
    private String contentType;

    /** "free_play" | "assignment" */
    private String sessionType;

    private int score;

    /**
     * Trạng thái đạt/không đạt.
     * Lưu dạng String để linh hoạt: "passed" | "failed" | "in_progress"
     */
    private String isPassed;

    /** Thời gian làm bài tính bằng giây */
    private int durationSeconds;

    @ServerTimestamp
    private Date startedAt;

    /** Nullable — null nghĩa là chưa hoàn thành */
    private Date completedAt;

    // Required by Firestore
    public ActivityAttempt() {}

    public ActivityAttempt(String childId, String contentId, String levelId, String sessionType, String contentType) {
        this.childId = childId;
        this.contentId = contentId;
        this.levelId = levelId;
        this.sessionType = sessionType;
        this.contentType = contentType;
        this.isPassed = "in_progress";
        this.score = 0;
        this.durationSeconds = 0;
    }

    // Getters & Setters
    public String getAttemptId() { return attemptId; }
    public void setAttemptId(String attemptId) { this.attemptId = attemptId; }

    public String getChildId() { return childId; }
    public void setChildId(String childId) { this.childId = childId; }

    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }

    public String getLevelId() { return levelId; }
    public void setLevelId(String levelId) { this.levelId = levelId; }

    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getIsPassed() { return isPassed; }
    public void setIsPassed(String isPassed) { this.isPassed = isPassed; }

    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }

    public Date getStartedAt() { return startedAt; }
    public void setStartedAt(Date startedAt) { this.startedAt = startedAt; }

    public Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }

    public boolean isCompleted() { return completedAt != null; }
    public boolean isPassed() { return "passed".equals(isPassed); }
}

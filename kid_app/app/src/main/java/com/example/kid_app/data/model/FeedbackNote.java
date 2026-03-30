package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model: feedback_notes (top-level collection)
 * Firestore path: /feedback_notes/{feedback_id}
 *
 * Top-level vì cần query:
 * - "Tất cả feedback của child X"
 * - "Feedback của giáo viên Y"
 */
public class FeedbackNote {

    @DocumentId
    private String feedbackId;

    private String childId;
    private String teacherId;

    /** Nullable — feedback gắn với content cụ thể */
    private String contentId;

    /** Nullable — feedback gắn với assignment cụ thể */
    private String assignmentId;

    private String noteText;

    /** Điểm mạnh của học sinh (nullable) */
    private String strengthTag;

    /** Điểm yếu cần cải thiện (nullable) */
    private String weaknessTag;

    @ServerTimestamp
    private Date createdAt;

    // Required by Firestore
    public FeedbackNote() {}

    public FeedbackNote(String childId, String teacherId, String noteText) {
        this.childId = childId;
        this.teacherId = teacherId;
        this.noteText = noteText;
    }

    // Getters & Setters
    public String getFeedbackId() { return feedbackId; }
    public void setFeedbackId(String feedbackId) { this.feedbackId = feedbackId; }

    public String getChildId() { return childId; }
    public void setChildId(String childId) { this.childId = childId; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }

    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }

    public String getNoteText() { return noteText; }
    public void setNoteText(String noteText) { this.noteText = noteText; }

    public String getStrengthTag() { return strengthTag; }
    public void setStrengthTag(String strengthTag) { this.strengthTag = strengthTag; }

    public String getWeaknessTag() { return weaknessTag; }
    public void setWeaknessTag(String weaknessTag) { this.weaknessTag = weaknessTag; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}

package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model: assignment_submissions (top-level collection)
 * Firestore path: /assignment_submissions/{submission_id}
 */
public class AssignmentSubmission {

    @DocumentId
    private String submissionId;

    private String assignmentId;
    private String childId;

    /** Nullable — ID của lần làm bài gần nhất */
    private String latestAttemptId;

    /** "not_started" | "in_progress" | "submitted" | "reviewed" */
    private String status;

    private int score;

    private Date completedAt;
    private Date reviewedAt;

    // Required by Firestore
    public AssignmentSubmission() {}

    public AssignmentSubmission(String assignmentId, String childId) {
        this.assignmentId = assignmentId;
        this.childId = childId;
        this.status = "not_started";
        this.score = 0;
    }

    // Getters & Setters
    public String getSubmissionId() { return submissionId; }
    public void setSubmissionId(String s) { submissionId = s; }
    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String s) { assignmentId = s; }
    public String getChildId() { return childId; }
    public void setChildId(String s) { childId = s; }
    public String getLatestAttemptId() { return latestAttemptId; }
    public void setLatestAttemptId(String s) { latestAttemptId = s; }
    public String getStatus() { return status; }
    public void setStatus(String s) { status = s; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(Date d) { completedAt = d; }
    public Date getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Date d) { reviewedAt = d; }
    public boolean isSubmitted() { return "submitted".equals(status) || "reviewed".equals(status); }
}

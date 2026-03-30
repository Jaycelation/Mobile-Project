package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model: assignments (top-level collection)
 * Firestore path: /assignments/{assignment_id}
 */
public class Assignment {

    @DocumentId
    private String assignmentId;

    private String classId;
    private String contentId;
    private String teacherId;
    private String title;
    private String instructions;
    private Date dueAt;

    /** 0=draft | 1=published | 2=closed */
    private int status;

    @ServerTimestamp
    private Date createdAt;

    // Required by Firestore
    public Assignment() {}

    public Assignment(String classId, String contentId, String teacherId, String title, Date dueAt) {
        this.classId = classId;
        this.contentId = contentId;
        this.teacherId = teacherId;
        this.title = title;
        this.dueAt = dueAt;
        this.status = 1;
    }

    // Getters & Setters
    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }
    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }
    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public Date getDueAt() { return dueAt; }
    public void setDueAt(Date dueAt) { this.dueAt = dueAt; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public boolean isPublished() { return status == 1; }
}

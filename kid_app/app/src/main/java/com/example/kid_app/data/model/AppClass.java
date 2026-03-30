package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model: classes (top-level collection)
 * Firestore path: /classes/{class_id}
 *
 * Đặt tên AppClass thay vì Class để tránh conflict với java.lang.Class.
 */
public class AppClass {

    @DocumentId
    private String classId;

    /** account_id của giáo viên sở hữu lớp */
    private String teacherId;

    private String className;
    private String gradeLevel;

    /** Mã ngắn để học sinh tham gia (vd: "ABC123") */
    private String joinCode;

    private String description;

    /** "active" | "archived" */
    private String status;

    @ServerTimestamp
    private Date createdAt;

    @ServerTimestamp
    private Date updatedAt;

    /** Nullable — soft delete */
    private Date deletedAt;

    // Required by Firestore
    public AppClass() {}

    public AppClass(String teacherId, String className, String gradeLevel, String joinCode) {
        this.teacherId = teacherId;
        this.className = className;
        this.gradeLevel = gradeLevel;
        this.joinCode = joinCode;
        this.status = "active";
    }

    // Getters & Setters
    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }

    public String getJoinCode() { return joinCode; }
    public void setJoinCode(String joinCode) { this.joinCode = joinCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Date getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Date deletedAt) { this.deletedAt = deletedAt; }

    public boolean isActive() { return "active".equals(status); }
}

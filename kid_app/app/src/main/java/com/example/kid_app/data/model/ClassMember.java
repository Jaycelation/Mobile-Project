package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model: class_members (top-level collection)
 * Firestore path: /class_members/{member_id}
 *
 * Top-level vì cần query hai chiều:
 * - "Tất cả thành viên của lớp X" → where classId == X
 * - "Bé Y đang học lớp nào" → where childId == Y
 */
public class ClassMember {

    @DocumentId
    private String memberId;

    private String classId;
    private String childId;

    /** account_id của phụ huynh đã cho bé tham gia lớp */
    private String joinedByParentId;

    /** "active" | "left" | "suspended" */
    private String memberStatus;

    @ServerTimestamp
    private Date joinedAt;

    /** Nullable — khi rời lớp */
    private Date leftAt;

    // Required by Firestore
    public ClassMember() {}

    public ClassMember(String classId, String childId, String joinedByParentId) {
        this.classId = classId;
        this.childId = childId;
        this.joinedByParentId = joinedByParentId;
        this.memberStatus = "active";
    }

    // Getters & Setters
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getChildId() { return childId; }
    public void setChildId(String childId) { this.childId = childId; }

    public String getJoinedByParentId() { return joinedByParentId; }
    public void setJoinedByParentId(String joinedByParentId) { this.joinedByParentId = joinedByParentId; }

    public String getMemberStatus() { return memberStatus; }
    public void setMemberStatus(String memberStatus) { this.memberStatus = memberStatus; }

    public Date getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Date joinedAt) { this.joinedAt = joinedAt; }

    public Date getLeftAt() { return leftAt; }
    public void setLeftAt(Date leftAt) { this.leftAt = leftAt; }

    public boolean isActive() { return "active".equals(memberStatus); }
}

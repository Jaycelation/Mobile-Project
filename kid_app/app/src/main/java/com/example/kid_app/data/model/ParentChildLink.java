package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model: parent_child_links (top-level collection)
 * Firestore path: /parent_child_links/{link_id}
 *
 * Top-level collection vì cần query hai chiều:
 * - "Tất cả con của parent X" → query where parentId == X
 * - "Tất cả parent của child Y" → query where childId == Y
 */
public class ParentChildLink {

    @DocumentId
    private String linkId;

    private String parentId;   // tham chiếu accounts/{account_id}
    private String childId;    // tham chiếu child_profiles/{child_id}

    /** "father" | "mother" | "guardian" | "other" */
    private String relationship;

    /** True nếu đây là phụ huynh chính (quyết định cài đặt) */
    private boolean isPrimary;

    @ServerTimestamp
    private Date createdAt;

    // Required by Firestore
    public ParentChildLink() {}

    public ParentChildLink(String parentId, String childId, String relationship, boolean isPrimary) {
        this.parentId = parentId;
        this.childId = childId;
        this.relationship = relationship;
        this.isPrimary = isPrimary;
    }

    // Getters & Setters
    public String getLinkId() { return linkId; }
    public void setLinkId(String linkId) { this.linkId = linkId; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getChildId() { return childId; }
    public void setChildId(String childId) { this.childId = childId; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean primary) { isPrimary = primary; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}

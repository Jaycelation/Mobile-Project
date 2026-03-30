package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model: ai_conversations (top-level collection)
 * Firestore path: /ai_conversations/{conversation_id}
 *
 * Top-level vì cần query "tất cả conversation của child X".
 * Tin nhắn lưu trong subcollection:
 *   /ai_conversations/{conversation_id}/messages/{message_id} → AiMessage
 */
public class AiConversation {

    @DocumentId
    private String conversationId;

    private String childId;

    /**
     * Ngữ cảnh cuộc trò chuyện.
     * "free_chat" | "quiz_help" | "game_help"
     * Xem AppConstants.AI_CONTEXT_*
     */
    private String contextType;

    /**
     * Nullable — mã tham chiếu ngữ cảnh.
     * Ví dụ: nếu contextType = "quiz_help" thì contextRefId = content_id của quiz đó.
     */
    private String contextRefId;

    @ServerTimestamp
    private Date createdAt;

    @ServerTimestamp
    private Date updatedAt;

    // Required by Firestore
    public AiConversation() {}

    public AiConversation(String childId, String contextType, String contextRefId) {
        this.childId = childId;
        this.contextType = contextType;
        this.contextRefId = contextRefId;
    }

    // Getters & Setters
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public String getChildId() { return childId; }
    public void setChildId(String childId) { this.childId = childId; }

    public String getContextType() { return contextType; }
    public void setContextType(String contextType) { this.contextType = contextType; }

    public String getContextRefId() { return contextRefId; }
    public void setContextRefId(String contextRefId) { this.contextRefId = contextRefId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}

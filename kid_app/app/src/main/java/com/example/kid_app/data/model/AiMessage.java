package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model: ai_messages (subcollection)
 * Firestore path: /ai_conversations/{conversation_id}/messages/{message_id}
 */
public class AiMessage {

    @DocumentId
    private String messageId;

    private String conversationId;  // redundant — để tiện log

    /**
     * Vai trò người gửi.
     * 0 = user (child), 1 = assistant (AI)
     * Xem AppConstants.AI_ROLE_USER / AI_ROLE_ASSISTANT
     */
    private int senderRole;

    private String messageText;

    /**
     * Nullable — nhãn an toàn nội dung.
     * null = chưa được kiểm duyệt | 0 = an toàn | 1 = cần xem xét | 2 = không phù hợp
     */
    private Integer safetyLabel;

    @ServerTimestamp
    private Date createdAt;

    // Required by Firestore
    public AiMessage() {}

    public AiMessage(String conversationId, int senderRole, String messageText) {
        this.conversationId = conversationId;
        this.senderRole = senderRole;
        this.messageText = messageText;
    }

    // Getters & Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public int getSenderRole() { return senderRole; }
    public void setSenderRole(int senderRole) { this.senderRole = senderRole; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public Integer getSafetyLabel() { return safetyLabel; }
    public void setSafetyLabel(Integer safetyLabel) { this.safetyLabel = safetyLabel; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public boolean isFromUser() { return senderRole == 0; }
    public boolean isFromAi() { return senderRole == 1; }
    public boolean isSafe() { return safetyLabel == null || safetyLabel == 0; }
}

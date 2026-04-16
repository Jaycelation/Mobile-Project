package com.example.kid_app.data.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class FeedbackMessage {
    private String senderId;
    private String senderRole; // "parent" or "teacher"
    private String messageText;
    @ServerTimestamp
    private Date createdAt;

    public FeedbackMessage() {}

    public FeedbackMessage(String senderId, String senderRole, String messageText) {
        this.senderId = senderId;
        this.senderRole = senderRole;
        this.messageText = messageText;
    }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}

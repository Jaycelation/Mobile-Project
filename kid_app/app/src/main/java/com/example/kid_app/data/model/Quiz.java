package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;

/**
 * Model: quizzes (subcollection)
 * Firestore path: /content_catalog/{content_id}/detail/quiz_detail
 *
 * Quan hệ 1-1 với ContentCatalog (content_type = "quiz").
 *
 * Câu hỏi lưu trong subcollection riêng:
 *   /content_catalog/{content_id}/questions/{question_id} → QuizQuestion
 */
public class Quiz {

    @DocumentId
    private String contentId;

    private String topic;

    /** Thời gian giới hạn cho toàn bài (giây). 0 = không giới hạn */
    private int timeLimitSeconds;

    /** Điểm tối thiểu để coi là "passed" */
    private int passScore;

    // Required by Firestore
    public Quiz() {}

    public Quiz(String contentId, String topic, int timeLimitSeconds, int passScore) {
        this.contentId = contentId;
        this.topic = topic;
        this.timeLimitSeconds = timeLimitSeconds;
        this.passScore = passScore;
    }

    // Getters & Setters
    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public int getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setTimeLimitSeconds(int timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }

    public int getPassScore() { return passScore; }
    public void setPassScore(int passScore) { this.passScore = passScore; }
}

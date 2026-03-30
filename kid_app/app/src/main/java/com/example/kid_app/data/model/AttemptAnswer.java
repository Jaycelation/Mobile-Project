package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model: attempt_answers (subcollection)
 * Firestore path:
 *   /child_profiles/{child_id}/activity_attempts/{attempt_id}/answers/{answer_id}
 *
 * Sub-subcollection: chỉ liên quan trong phạm vi một attempt cụ thể.
 * Không cần query cross-attempt toàn cục thường xuyên.
 */
public class AttemptAnswer {

    @DocumentId
    private String answerId;

    private String attemptId;   // redundant — để tiện log
    private String questionId;  // tham chiếu QuizQuestion

    /** Index của đáp án đã chọn trong mảng options (0-based) */
    private int selectedAnswerIndex;

    /** Text đáp án đã chọn (lưu để dễ hiển thị lại) */
    private String selectedAnswerText;

    private boolean isCorrect;

    /** Thời gian phản hồi tính bằng millisecond */
    private int responseTimeMs;

    @ServerTimestamp
    private Date answeredAt;

    // Required by Firestore
    public AttemptAnswer() {}

    public AttemptAnswer(String attemptId, String questionId,
                         int selectedAnswerIndex, String selectedAnswerText,
                         boolean isCorrect, int responseTimeMs) {
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.selectedAnswerIndex = selectedAnswerIndex;
        this.selectedAnswerText = selectedAnswerText;
        this.isCorrect = isCorrect;
        this.responseTimeMs = responseTimeMs;
    }

    // Getters & Setters
    public String getAnswerId() { return answerId; }
    public void setAnswerId(String answerId) { this.answerId = answerId; }

    public String getAttemptId() { return attemptId; }
    public void setAttemptId(String attemptId) { this.attemptId = attemptId; }

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public int getSelectedAnswerIndex() { return selectedAnswerIndex; }
    public void setSelectedAnswerIndex(int selectedAnswerIndex) { this.selectedAnswerIndex = selectedAnswerIndex; }

    public String getSelectedAnswerText() { return selectedAnswerText; }
    public void setSelectedAnswerText(String selectedAnswerText) { this.selectedAnswerText = selectedAnswerText; }

    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }

    public int getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(int responseTimeMs) { this.responseTimeMs = responseTimeMs; }

    public Date getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(Date answeredAt) { this.answeredAt = answeredAt; }
}

package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;

import java.util.List;

/**
 * Model: quiz_questions (subcollection)
 * Firestore path: /content_catalog/{content_id}/questions/{question_id}
 *
 * Subcollection vì câu hỏi chỉ có nghĩa trong ngữ cảnh quiz cụ thể.
 * options lưu dưới dạng List<String> để Firestore tự serialize.
 */
public class QuizQuestion {

    @DocumentId
    private String questionId;

    private String contentId;  // redundant — để biết quiz cha

    /** Nullable — null nghĩa là câu hỏi chung, không gắn level */
    private String levelId;

    private String questionText;

    /**
     * Danh sách các phương án trả lời.
     * Ví dụ: ["Đỏ", "Xanh", "Vàng", "Tím"]
     */
    private List<String> options;

    /**
     * Đáp án đúng — lưu dạng text khớp với một phần tử trong options.
     * Ví dụ: "Xanh"
     */
    private String correctAnswer;

    /** Giải thích đáp án (nullable — có thể để null) */
    private String explanation;

    /** URL hình ảnh/âm thanh minh họa câu hỏi (nullable) */
    private String mediaUrl;

    // Required by Firestore
    public QuizQuestion() {}

    public QuizQuestion(String contentId, String questionText, List<String> options, String correctAnswer) {
        this.contentId = contentId;
        this.questionText = questionText;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    // Getters & Setters
    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }

    public String getLevelId() { return levelId; }
    public void setLevelId(String levelId) { this.levelId = levelId; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    /** Kiểm tra xem đáp án đã chọn có đúng không */
    public boolean isCorrect(String selectedAnswer) {
        return correctAnswer != null && correctAnswer.equals(selectedAnswer);
    }
}

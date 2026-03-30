package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;

/**
 * Model: games (subcollection)
 * Firestore path: /content_catalog/{content_id}/detail/game_detail
 *
 * Quan hệ 1-1 với ContentCatalog (content_type = "game").
 * Lưu thông tin đặc thù của game.
 */
public class Game {

    @DocumentId
    private String contentId;  // trùng với contentId cha

    /**
     * Loại game — dùng để màn hình biết render engine nào.
     * Ví dụ: "pattern_sequence" | "quick_tap" | "memory_match"
     */
    private String gameType;

    /** Mô tả mục tiêu game (hiển thị trước khi chơi) */
    private String goalDesc;

    /** 0 = không giới hạn thời gian */
    private int timeLimitSeconds;

    /** Số mạng. 0 = không giới hạn */
    private int maxLives;

    // Required by Firestore
    public Game() {}

    public Game(String contentId, String gameType, int timeLimitSeconds, int maxLives) {
        this.contentId = contentId;
        this.gameType = gameType;
        this.timeLimitSeconds = timeLimitSeconds;
        this.maxLives = maxLives;
    }

    // Getters & Setters
    public String getContentId() { return contentId; }
    public void setContentId(String contentId) { this.contentId = contentId; }

    public String getGameType() { return gameType; }
    public void setGameType(String gameType) { this.gameType = gameType; }

    public String getGoalDesc() { return goalDesc; }
    public void setGoalDesc(String goalDesc) { this.goalDesc = goalDesc; }

    public int getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setTimeLimitSeconds(int timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }

    public int getMaxLives() { return maxLives; }
    public void setMaxLives(int maxLives) { this.maxLives = maxLives; }
}

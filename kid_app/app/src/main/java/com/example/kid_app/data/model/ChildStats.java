package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model: child_stats (top-level collection)
 * Firestore path: /child_stats/{child_id}
 *
 * Dùng child_id làm document ID (quan hệ 1-1 với ChildProfile).
 * Top-level thay vì subcollection vì cần query/aggregate theo class/leaderboard.
 */
public class ChildStats {

    @DocumentId
    private String childId;

    private int totalPoints;
    private int streakDays;
    private int totalCompleted;
    private int totalPlayTimeSeconds;

    private Date lastActiveAt;

    @ServerTimestamp
    private Date updatedAt;

    // Required by Firestore
    public ChildStats() {}

    public ChildStats(String childId) {
        this.childId = childId;
        this.totalPoints = 0;
        this.streakDays = 0;
        this.totalCompleted = 0;
        this.totalPlayTimeSeconds = 0;
    }

    // Getters & Setters
    public String getChildId() { return childId; }
    public void setChildId(String childId) { this.childId = childId; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public int getStreakDays() { return streakDays; }
    public void setStreakDays(int streakDays) { this.streakDays = streakDays; }

    public int getTotalCompleted() { return totalCompleted; }
    public void setTotalCompleted(int totalCompleted) { this.totalCompleted = totalCompleted; }

    public int getTotalPlayTimeSeconds() { return totalPlayTimeSeconds; }
    public void setTotalPlayTimeSeconds(int totalPlayTimeSeconds) { this.totalPlayTimeSeconds = totalPlayTimeSeconds; }

    public Date getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(Date lastActiveAt) { this.lastActiveAt = lastActiveAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}

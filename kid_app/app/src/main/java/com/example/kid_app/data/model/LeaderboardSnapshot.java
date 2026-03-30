package com.example.kid_app.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Model: leaderboard_snapshots (top-level collection)
 * Firestore path: /leaderboard_snapshots/{snapshot_id}
 *
 * Snapshot được generate định kỳ (hàng tuần/tháng) thay vì real-time
 * để tránh đọc/ghi quá nhiều lên child_stats.
 */
public class LeaderboardSnapshot {

    @DocumentId
    private String snapshotId;

    private String classId;

    /** "weekly" | "monthly" | "all_time" */
    private String periodType;

    /** Ví dụ: "2024-W12" (tuần 12 năm 2024) | "2024-03" (tháng 3/2024) */
    private String periodValue;

    /**
     * Dữ liệu xếp hạng dạng JSON string.
     * Ví dụ: [{"rank":1,"childId":"abc","name":"An","points":500}, ...]
     * Lưu dạng String để linh hoạt thay đổi format sau.
     */
    private String rankingsData;

    @ServerTimestamp
    private Date generatedAt;

    // Required by Firestore
    public LeaderboardSnapshot() {}

    public LeaderboardSnapshot(String classId, String periodType, String periodValue) {
        this.classId = classId;
        this.periodType = periodType;
        this.periodValue = periodValue;
    }

    // Getters & Setters
    public String getSnapshotId() { return snapshotId; }
    public void setSnapshotId(String s) { snapshotId = s; }
    public String getClassId() { return classId; }
    public void setClassId(String s) { classId = s; }
    public String getPeriodType() { return periodType; }
    public void setPeriodType(String s) { periodType = s; }
    public String getPeriodValue() { return periodValue; }
    public void setPeriodValue(String s) { periodValue = s; }
    public String getRankingsData() { return rankingsData; }
    public void setRankingsData(String s) { rankingsData = s; }
    public Date getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Date d) { generatedAt = d; }
}

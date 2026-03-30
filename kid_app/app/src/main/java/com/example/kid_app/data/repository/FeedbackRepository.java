package com.example.kid_app.data.repository;

import com.example.kid_app.common.AppConstants;
import com.example.kid_app.data.FirestoreHelper;
import com.example.kid_app.data.model.FeedbackNote;
import com.example.kid_app.data.model.LeaderboardSnapshot;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * FeedbackRepository — thao tác với:
 * - /feedback_notes/{feedback_id}           (top-level — phản hồi của giáo viên)
 * - /leaderboard_snapshots/{snapshot_id}    (top-level — bảng xếp hạng snapshot)
 *
 * Tại sao feedback_notes là top-level:
 * - Cần query 2 chiều: "phản hồi giáo viên X gửi" VÀ "phản hồi bé Y nhận được".
 * - Cũng cần query theo content_id hoặc assignment_id → top-level + index phù hợp.
 *
 * Tại sao leaderboard_snapshots là top-level:
 * - Snapshot được tạo định kỳ (Cloud Function hoặc batch), không gắn với một entity duy nhất.
 * - Cần query theo class + period_type + generated_at → top-level + composite index.
 */
public class FeedbackRepository {

    private final FirebaseFirestore db;

    public FeedbackRepository() {
        this.db = FirestoreHelper.getInstance().getFirestore();
    }

    // ==================== FEEDBACK NOTES ====================

    /**
     * Giáo viên tạo phản hồi cho bé.
     * Trả về DocumentReference để lấy feedbackId.
     */
    public Task<DocumentReference> createFeedback(FeedbackNote note) {
        return db.collection(AppConstants.COL_FEEDBACK_NOTES)
                .add(note);
    }

    /** Lấy phản hồi theo id */
    public Task<DocumentSnapshot> getFeedbackById(String feedbackId) {
        return db.collection(AppConstants.COL_FEEDBACK_NOTES)
                .document(feedbackId)
                .get();
    }

    /**
     * Lấy tất cả phản hồi của một bé — hiển thị cho parent/child.
     * Sắp xếp mới nhất trước.
     */
    public Task<QuerySnapshot> getFeedbackForChild(String childId) {
        return db.collection(AppConstants.COL_FEEDBACK_NOTES)
                .whereEqualTo("childId", childId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Lấy phản hồi giáo viên đã gửi — dùng trong màn hình teacher dashboard.
     */
    public Task<QuerySnapshot> getFeedbackByTeacher(String teacherId) {
        return db.collection(AppConstants.COL_FEEDBACK_NOTES)
                .whereEqualTo("teacherId", teacherId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Lấy phản hồi liên quan đến một bài tập cụ thể.
     */
    public Task<QuerySnapshot> getFeedbackForAssignment(String childId, String assignmentId) {
        return db.collection(AppConstants.COL_FEEDBACK_NOTES)
                .whereEqualTo("childId", childId)
                .whereEqualTo("assignmentId", assignmentId)
                .get();
    }

    // ==================== LEADERBOARD SNAPSHOTS ====================

    /**
     * Lưu một leaderboard snapshot mới.
     * Thường được gọi từ Cloud Function hoặc batch job cuối tuần/tháng.
     * Trả về DocumentReference để lấy snapshotId.
     */
    public Task<DocumentReference> saveSnapshot(LeaderboardSnapshot snapshot) {
        return db.collection(AppConstants.COL_LEADERBOARD_SNAPSHOTS)
                .add(snapshot);
    }

    /**
     * Lấy snapshot mới nhất của một lớp trong kỳ hiện tại.
     * periodType: "weekly" | "monthly" | "all_time"
     */
    public Task<QuerySnapshot> getLatestSnapshot(String classId, String periodType) {
        return db.collection(AppConstants.COL_LEADERBOARD_SNAPSHOTS)
                .whereEqualTo("classId", classId)
                .whereEqualTo("periodType", periodType)
                .orderBy("generatedAt", Query.Direction.DESCENDING)
                .limit(1)
                .get();
    }

    /**
     * Lấy lịch sử snapshot của một lớp theo kỳ.
     * Dùng để vẽ biểu đồ tiến độ theo thời gian.
     */
    public Task<QuerySnapshot> getSnapshotHistory(String classId, String periodType, int limit) {
        return db.collection(AppConstants.COL_LEADERBOARD_SNAPSHOTS)
                .whereEqualTo("classId", classId)
                .whereEqualTo("periodType", periodType)
                .orderBy("generatedAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
    }
}

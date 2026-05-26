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
    // Chuc nang: giao vien tao phan hoi ca nhan cho tre.
    public Task<DocumentReference> createFeedback(FeedbackNote note) {
        return db.collection(AppConstants.COL_FEEDBACK_NOTES)
                .add(note);
    }

    /** Lấy phản hồi theo id */
    // Chuc nang: doc chi tiet mot phan hoi theo id.
    public Task<DocumentSnapshot> getFeedbackById(String feedbackId) {
        return db.collection(AppConstants.COL_FEEDBACK_NOTES)
                .document(feedbackId)
                .get();
    }

    /**
     * Lấy tất cả phản hồi của một bé — hiển thị cho parent/child.
     * Sắp xếp mới nhất trước.
     */
    // Chuc nang: lay tat ca phan hoi ma mot tre nhan duoc.
    public Task<QuerySnapshot> getFeedbackForChild(String childId) {
        return db.collection(AppConstants.COL_FEEDBACK_NOTES)
                .whereEqualTo("childId", childId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Lấy phản hồi giáo viên đã gửi — dùng trong màn hình teacher dashboard.
     */
    // Chuc nang: lay cac phan hoi do mot giao vien da gui.
    public Task<QuerySnapshot> getFeedbackByTeacher(String teacherId) {
        return db.collection(AppConstants.COL_FEEDBACK_NOTES)
                .whereEqualTo("teacherId", teacherId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Lấy phản hồi liên quan đến một bài tập cụ thể.
     */
    // Chuc nang: lay phan hoi lien quan den mot bai tap cu the.
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
    // Chuc nang: luu snapshot bang xep hang de hien thi thanh tich theo ky.
    public Task<DocumentReference> saveSnapshot(LeaderboardSnapshot snapshot) {
        return db.collection(AppConstants.COL_LEADERBOARD_SNAPSHOTS)
                .add(snapshot);
    }

    /**
     * Lấy snapshot mới nhất của một lớp trong kỳ hiện tại.
     * periodType: "weekly" | "monthly" | "all_time"
     */
    // Chuc nang: lay bang xep hang moi nhat cua lop theo tuan, thang hoac toan thoi gian.
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
    // Chuc nang: lay lich su bang xep hang de phuc vu thong ke tien do.
    public Task<QuerySnapshot> getSnapshotHistory(String classId, String periodType, int limit) {
        return db.collection(AppConstants.COL_LEADERBOARD_SNAPSHOTS)
                .whereEqualTo("classId", classId)
                .whereEqualTo("periodType", periodType)
                .orderBy("generatedAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
    }
}

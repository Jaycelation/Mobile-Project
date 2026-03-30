package com.example.kid_app.data.repository;

import com.example.kid_app.common.AppConstants;
import com.example.kid_app.data.FirestoreHelper;
import com.example.kid_app.data.model.ActivityAttempt;
import com.example.kid_app.data.model.AttemptAnswer;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * ActivityAttemptRepository — thao tác với:
 * - /child_profiles/{child_id}/activity_attempts/{attempt_id}   (subcollection)
 * - /child_profiles/{child_id}/activity_attempts/{attempt_id}/answers/{answer_id}  (nested sub)
 *
 * Lý do dùng subcollection thay vì top-level collection:
 * - Query phổ biến nhất là "tất cả attempt của child X" → subcollection tự nhiên.
 * - Nếu cần aggregate toàn hệ thống (admin), dùng:
 *   db.collectionGroup(SUBCOL_ACTIVITY_ATTEMPTS).get()
 */
public class ActivityAttemptRepository {

    private final FirebaseFirestore db;

    public ActivityAttemptRepository() {
        this.db = FirestoreHelper.getInstance().getFirestore();
    }

    // ==================== ACTIVITY ATTEMPTS ====================

    /**
     * Tạo attempt mới khi bé bắt đầu làm bài.
     * Trả về DocumentReference để lấy attemptId sinh ra.
     */
    public Task<DocumentReference> startAttempt(String childId, ActivityAttempt attempt) {
        return db.collection(AppConstants.COL_CHILD_PROFILES)
                .document(childId)
                .collection(AppConstants.SUBCOL_ACTIVITY_ATTEMPTS)
                .add(attempt);
    }

    /** Lấy attempt theo id */
    public Task<DocumentSnapshot> getAttempt(String childId, String attemptId) {
        return db.collection(AppConstants.COL_CHILD_PROFILES)
                .document(childId)
                .collection(AppConstants.SUBCOL_ACTIVITY_ATTEMPTS)
                .document(attemptId)
                .get();
    }

    /**
     * Cập nhật kết quả khi bé hoàn thành bài.
     * Chỉ update các field cần thiết, không ghi đè toàn bộ document.
     */
    public Task<Void> completeAttempt(String childId,
                                      String attemptId,
                                      int score,
                                      String isPassed,
                                      int durationSeconds) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("score", score);
        updates.put("isPassed", isPassed);
        updates.put("durationSeconds", durationSeconds);
        updates.put("completedAt", new Date());

        return db.collection(AppConstants.COL_CHILD_PROFILES)
                .document(childId)
                .collection(AppConstants.SUBCOL_ACTIVITY_ATTEMPTS)
                .document(attemptId)
                .update(updates);
    }

    /**
     * Lấy lịch sử attempt gần đây nhất của bé cho một nội dung cụ thể.
     * Hữu ích để kiểm tra xem bé đã hoàn thành level này chưa.
     */
    public Task<QuerySnapshot> getAttemptsByContent(String childId, String contentId) {
        return db.collection(AppConstants.COL_CHILD_PROFILES)
                .document(childId)
                .collection(AppConstants.SUBCOL_ACTIVITY_ATTEMPTS)
                .whereEqualTo("contentId", contentId)
                .orderBy("startedAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Lấy tất cả attempt gần đây của bé (trang lịch sử học tập).
     * Giới hạn 20 để tránh tải quá nhiều.
     */
    public Task<QuerySnapshot> getRecentAttempts(String childId, int limit) {
        return db.collection(AppConstants.COL_CHILD_PROFILES)
                .document(childId)
                .collection(AppConstants.SUBCOL_ACTIVITY_ATTEMPTS)
                .orderBy("startedAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
    }

    /**
     * Lấy attempt đã pass của bé — dùng để kiểm tra điều kiện unlock level tiếp theo.
     */
    public Task<QuerySnapshot> getPassedAttemptsByContent(String childId, String contentId) {
        return db.collection(AppConstants.COL_CHILD_PROFILES)
                .document(childId)
                .collection(AppConstants.SUBCOL_ACTIVITY_ATTEMPTS)
                .whereEqualTo("contentId", contentId)
                .whereEqualTo("isPassed", "passed")
                .get();
    }

    // ==================== ATTEMPT ANSWERS ====================

    /**
     * Lưu câu trả lời của bé cho một câu hỏi trong attempt.
     * Path: /child_profiles/{child_id}/activity_attempts/{attempt_id}/answers/{answer_id}
     */
    public Task<DocumentReference> saveAnswer(String childId,
                                               String attemptId,
                                               AttemptAnswer answer) {
        return db.collection(AppConstants.COL_CHILD_PROFILES)
                .document(childId)
                .collection(AppConstants.SUBCOL_ACTIVITY_ATTEMPTS)
                .document(attemptId)
                .collection(AppConstants.SUBCOL_ATTEMPT_ANSWERS)
                .add(answer);
    }

    /** Lấy tất cả câu trả lời của một attempt */
    public Task<QuerySnapshot> getAnswers(String childId, String attemptId) {
        return db.collection(AppConstants.COL_CHILD_PROFILES)
                .document(childId)
                .collection(AppConstants.SUBCOL_ACTIVITY_ATTEMPTS)
                .document(attemptId)
                .collection(AppConstants.SUBCOL_ATTEMPT_ANSWERS)
                .get();
    }
}

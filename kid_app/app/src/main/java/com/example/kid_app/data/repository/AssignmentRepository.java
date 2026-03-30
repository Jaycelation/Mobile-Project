package com.example.kid_app.data.repository;

import com.example.kid_app.common.AppConstants;
import com.example.kid_app.data.FirestoreHelper;
import com.example.kid_app.data.model.Assignment;
import com.example.kid_app.data.model.AssignmentSubmission;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * AssignmentRepository — thao tác với:
 * - /assignments/{assignment_id}
 * - /assignment_submissions/{submission_id}
 */
public class AssignmentRepository {

    private final FirebaseFirestore db;

    public AssignmentRepository() {
        this.db = FirestoreHelper.getInstance().getFirestore();
    }

    // ==================== ASSIGNMENTS ====================

    /** Giáo viên tạo bài tập mới */
    public Task<DocumentReference> createAssignment(Assignment assignment) {
        return db.collection(AppConstants.COL_ASSIGNMENTS)
                .add(assignment);
    }

    public Task<DocumentSnapshot> getAssignment(String assignmentId) {
        return db.collection(AppConstants.COL_ASSIGNMENTS)
                .document(assignmentId)
                .get();
    }

    /** Lấy tất cả bài tập của một lớp */
    public Task<QuerySnapshot> getAssignmentsByClass(String classId) {
        return db.collection(AppConstants.COL_ASSIGNMENTS)
                .whereEqualTo("classId", classId)
                .whereEqualTo("status", 1) // 1=published
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /** Lấy bài tập của giáo viên */
    public Task<QuerySnapshot> getAssignmentsByTeacher(String teacherId) {
        return db.collection(AppConstants.COL_ASSIGNMENTS)
                .whereEqualTo("teacherId", teacherId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    public Task<Void> updateAssignmentStatus(String assignmentId, int status) {
        return db.collection(AppConstants.COL_ASSIGNMENTS)
                .document(assignmentId)
                .update("status", status);
    }

    // ==================== ASSIGNMENT SUBMISSIONS ====================

    /** Tạo submission record khi bé bắt đầu làm bài */
    public Task<DocumentReference> createSubmission(AssignmentSubmission submission) {
        return db.collection(AppConstants.COL_ASSIGNMENT_SUBMISSIONS)
                .add(submission);
    }

    /** Lấy submission của bé cho bài cụ thể */
    public Task<QuerySnapshot> getSubmission(String assignmentId, String childId) {
        return db.collection(AppConstants.COL_ASSIGNMENT_SUBMISSIONS)
                .whereEqualTo("assignmentId", assignmentId)
                .whereEqualTo("childId", childId)
                .limit(1)
                .get();
    }

    /** Cập nhật kết quả submission sau khi bé nộp bài */
    public Task<Void> submitAssignment(String submissionId, String latestAttemptId, int score) {
        return db.collection(AppConstants.COL_ASSIGNMENT_SUBMISSIONS)
                .document(submissionId)
                .update(
                    "status", "submitted",
                    "latestAttemptId", latestAttemptId,
                    "score", score,
                    "completedAt", new java.util.Date()
                );
    }

    /** Giáo viên đánh giá/review bài nộp */
    public Task<Void> reviewSubmission(String submissionId) {
        return db.collection(AppConstants.COL_ASSIGNMENT_SUBMISSIONS)
                .document(submissionId)
                .update("status", "reviewed",
                        "reviewedAt", new java.util.Date());
    }

    /** Lấy tất cả submission của một assignment (giáo viên xem) */
    public Task<QuerySnapshot> getSubmissionsForAssignment(String assignmentId) {
        return db.collection(AppConstants.COL_ASSIGNMENT_SUBMISSIONS)
                .whereEqualTo("assignmentId", assignmentId)
                .get();
    }

    /** Lấy tất cả submission của bé (lịch sử bài làm) */
    public Task<QuerySnapshot> getSubmissionsForChild(String childId) {
        return db.collection(AppConstants.COL_ASSIGNMENT_SUBMISSIONS)
                .whereEqualTo("childId", childId)
                .orderBy("completedAt", Query.Direction.DESCENDING)
                .get();
    }
}

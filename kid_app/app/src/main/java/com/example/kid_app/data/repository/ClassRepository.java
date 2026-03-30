package com.example.kid_app.data.repository;

import com.example.kid_app.common.AppConstants;
import com.example.kid_app.data.FirestoreHelper;
import com.example.kid_app.data.model.AppClass;
import com.example.kid_app.data.model.ClassMember;
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
 * ClassRepository — thao tác với:
 * - /classes/{class_id}           (top-level — lớp học)
 * - /class_members/{member_id}    (top-level — thành viên lớp)
 *
 * Tại sao class_members là top-level (không phải subcollection dưới classes):
 * - Cần query 2 chiều: "tất cả lớp của child X" VÀ "tất cả child trong lớp Y".
 * - Query 2 chiều trên subcollection không tự nhiên → top-level + composite index.
 */
public class ClassRepository {

    private final FirebaseFirestore db;

    public ClassRepository() {
        this.db = FirestoreHelper.getInstance().getFirestore();
    }

    // ==================== CLASSES ====================

    /**
     * Giáo viên tạo lớp mới.
     * Trả về DocumentReference để lấy classId tự sinh.
     */
    public Task<DocumentReference> createClass(AppClass appClass) {
        return db.collection(AppConstants.COL_CLASSES)
                .add(appClass);
    }

    /** Lấy lớp theo id */
    public Task<DocumentSnapshot> getClassById(String classId) {
        return db.collection(AppConstants.COL_CLASSES)
                .document(classId)
                .get();
    }

    /**
     * Lấy lớp theo join code — dùng khi parent nhập code để đăng ký cho bé.
     * Kết quả: nếu QuerySnapshot.isEmpty() → không tìm thấy lớp.
     */
    public Task<QuerySnapshot> getClassByJoinCode(String joinCode) {
        return db.collection(AppConstants.COL_CLASSES)
                .whereEqualTo("joinCode", joinCode)
                .whereEqualTo("status", AppConstants.STATUS_ACTIVE)
                .limit(1)
                .get();
    }

    /** Lấy tất cả lớp của một giáo viên */
    public Task<QuerySnapshot> getClassesByTeacher(String teacherId) {
        return db.collection(AppConstants.COL_CLASSES)
                .whereEqualTo("teacherId", teacherId)
                .whereEqualTo("status", AppConstants.STATUS_ACTIVE)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /** Cập nhật thông tin lớp */
    public Task<Void> updateClass(String classId, AppClass appClass) {
        return db.collection(AppConstants.COL_CLASSES)
                .document(classId)
                .set(appClass);
    }

    /** Soft delete lớp học */
    public Task<Void> softDeleteClass(String classId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", AppConstants.STATUS_DELETED);
        updates.put("deletedAt", new Date());
        return db.collection(AppConstants.COL_CLASSES)
                .document(classId)
                .update(updates);
    }

    // ==================== CLASS MEMBERS ====================

    /**
     * Thêm bé vào lớp (parent dùng join code).
     * Trả về DocumentReference để lấy memberId.
     */
    public Task<DocumentReference> addMember(ClassMember member) {
        return db.collection(AppConstants.COL_CLASS_MEMBERS)
                .add(member);
    }

    /** Lấy tất cả thành viên của một lớp (giáo viên xem) */
    public Task<QuerySnapshot> getMembersOfClass(String classId) {
        return db.collection(AppConstants.COL_CLASS_MEMBERS)
                .whereEqualTo("classId", classId)
                .whereEqualTo("memberStatus", "active")
                .get();
    }

    /**
     * Lấy tất cả lớp mà một bé tham gia.
     * Kết quả là danh sách ClassMember → từ đó lấy classId.
     */
    public Task<QuerySnapshot> getClassesOfChild(String childId) {
        return db.collection(AppConstants.COL_CLASS_MEMBERS)
                .whereEqualTo("childId", childId)
                .whereEqualTo("memberStatus", "active")
                .get();
    }

    /**
     * Kiểm tra bé đã tham gia lớp chưa — tránh thêm trùng.
     */
    public Task<QuerySnapshot> checkChildInClass(String classId, String childId) {
        return db.collection(AppConstants.COL_CLASS_MEMBERS)
                .whereEqualTo("classId", classId)
                .whereEqualTo("childId", childId)
                .limit(1)
                .get();
    }

    /**
     * Bé/phụ huynh rời khỏi lớp — soft delete bằng cách update leftAt và status.
     */
    public Task<Void> removeMember(String memberId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("memberStatus", "left");
        updates.put("leftAt", new Date());
        return db.collection(AppConstants.COL_CLASS_MEMBERS)
                .document(memberId)
                .update(updates);
    }
}

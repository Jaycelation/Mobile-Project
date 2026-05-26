package com.example.kid_app.data.repository;

import com.example.kid_app.common.AppConstants;
import com.example.kid_app.data.FirestoreHelper;
import com.example.kid_app.data.model.Badge;
import com.example.kid_app.data.model.ChildBadge;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * BadgeRepository — thao tác với:
 * - /badges/{badge_id}         (top-level — định nghĩa huy hiệu toàn hệ thống)
 * - /child_badges/{child_badge_id}  (top-level — huy hiệu của từng bé)
 *
 * Lý do badges và child_badges là top-level (không phải subcollection):
 * - Badges cần query độc lập: "tất cả badge loại X" không phụ thuộc child.
 * - child_badges cần query theo childId lẫn badgeId → top-level + composite index.
 */
public class BadgeRepository {

    private final FirebaseFirestore db;

    public BadgeRepository() {
        this.db = FirestoreHelper.getInstance().getFirestore();
    }

    // ==================== BADGE DEFINITIONS (toàn hệ thống) ====================

    /** Admin tạo badge mới */
    // Chuc nang: tao dinh nghia huy hieu moi trong Firestore.
    public Task<Void> createBadge(String badgeId, Badge badge) {
        return db.collection(AppConstants.COL_BADGES)
                .document(badgeId)
                .set(badge);
    }

    /** Lấy tất cả badge active */
    // Chuc nang: lay tat ca huy hieu dang hoat dong de hien thi cho tre.
    public Task<QuerySnapshot> getAllBadges() {
        return db.collection(AppConstants.COL_BADGES)
                .whereEqualTo("isActive", true)
                .get();
    }

    /** Lấy badge theo id */
    // Chuc nang: doc thong tin chi tiet cua mot huy hieu.
    public Task<DocumentSnapshot> getBadgeById(String badgeId) {
        return db.collection(AppConstants.COL_BADGES)
                .document(badgeId)
                .get();
    }

    /**
     * Lấy badge theo loại tiêu chí.
     * criteriaType: "total_points" | "streak_days" | "content_complete" | ...
     */
    // Chuc nang: loc huy hieu theo loai dieu kien nhan.
    public Task<QuerySnapshot> getBadgesByCriteriaType(String criteriaType) {
        return db.collection(AppConstants.COL_BADGES)
                .whereEqualTo("criteriaType", criteriaType)
                .whereEqualTo("isActive", true)
                .get();
    }

    // ==================== CHILD BADGES (huy hiệu của bé) ====================

    /**
     * Trao huy hiệu cho bé.
     * Trả về DocumentReference để lấy childBadgeId sinh ra.
     */
    // Chuc nang: cap huy hieu cho tre va luu ban ghi vao child_badges.
    public Task<DocumentReference> awardBadge(ChildBadge childBadge) {
        return db.collection(AppConstants.COL_CHILD_BADGES)
                .add(childBadge);
    }

    /** Lấy tất cả huy hiệu của một bé */
    // Chuc nang: lay danh sach huy hieu ma tre da dat duoc.
    public Task<QuerySnapshot> getChildBadges(String childId) {
        return db.collection(AppConstants.COL_CHILD_BADGES)
                .whereEqualTo("childId", childId)
                .orderBy("awardedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Kiểm tra bé đã có badge cụ thể chưa — tránh trao trùng.
     * Kết quả: nếu QuerySnapshot.isEmpty() → chưa có, có thể trao.
     */
    // Chuc nang: kiem tra tre da co huy hieu nay chua de tranh cap trung.
    public Task<QuerySnapshot> checkChildHasBadge(String childId, String badgeId) {
        return db.collection(AppConstants.COL_CHILD_BADGES)
                .whereEqualTo("childId", childId)
                .whereEqualTo("badgeId", badgeId)
                .limit(1)
                .get();
    }

    /**
     * Lấy badge bé vừa nhận từ một nguồn cụ thể.
     * sourceType: "game" | "quiz" | "color_activity" | "counting_activity"
     */
    // Chuc nang: lay huy hieu duoc cap tu mot nguon nhu game, quiz hoac bai hoc.
    public Task<QuerySnapshot> getBadgesBySource(String childId, String sourceType, String sourceId) {
        return db.collection(AppConstants.COL_CHILD_BADGES)
                .whereEqualTo("childId", childId)
                .whereEqualTo("sourceType", sourceType)
                .whereEqualTo("sourceId", sourceId)
                .get();
    }
}

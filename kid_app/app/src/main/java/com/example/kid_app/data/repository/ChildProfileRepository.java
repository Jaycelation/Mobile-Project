package com.example.kid_app.data.repository;

import com.example.kid_app.common.AppConstants;
import com.example.kid_app.data.FirestoreHelper;
import com.example.kid_app.data.model.ChildProfile;
import com.example.kid_app.data.model.ChildSettings;
import com.example.kid_app.data.model.ChildStats;
import com.example.kid_app.data.model.ParentChildLink;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

/**
 * ChildProfileRepository — thao tác với:
 * - /child_profiles/{child_id}
 * - /child_profiles/{child_id}/settings/child_settings
 * - /parent_child_links/
 * - /child_stats/{child_id}
 */
public class ChildProfileRepository {

    private final FirebaseFirestore db;

    public ChildProfileRepository() {
        this.db = FirestoreHelper.getInstance().getFirestore();
    }

    // ==================== CHILD PROFILE ====================

    /** Tạo hồ sơ bé mới và khởi tạo stats */
    public Task<Void> createChildProfile(String childId, ChildProfile profile) {
        return db.collection(AppConstants.COL_CHILD_PROFILES)
                .document(childId)
                .set(profile);
    }

    public Task<DocumentSnapshot> getChildProfile(String childId) {
        return db.collection(AppConstants.COL_CHILD_PROFILES)
                .document(childId)
                .get();
    }

    public Task<Void> updateChildProfile(String childId, ChildProfile profile) {
        return db.collection(AppConstants.COL_CHILD_PROFILES)
                .document(childId)
                .set(profile);
    }

    /** Xóa sạch dữ liệu của bé khỏi hệ thống (Dành cho Phụ huynh) */
    public Task<Void> hardDeleteChildProfile(String childId) {
        WriteBatch batch = db.batch();

        // 1. Xóa trong child_profiles
        batch.delete(db.collection(AppConstants.COL_CHILD_PROFILES).document(childId));

        // 2. Xóa trong child_stats
        batch.delete(db.collection(AppConstants.COL_CHILD_STATS).document(childId));

        // Thực hiện chuỗi xóa các collection liên quan
        return db.collection(AppConstants.COL_PARENT_CHILD_LINKS)
                .whereEqualTo("childId", childId)
                .get()
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()) batch.delete(doc.getReference());
                    }
                    return db.collection(AppConstants.COL_ASSIGNMENT_SUBMISSIONS)
                            .whereEqualTo("childId", childId)
                            .get();
                })
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()) batch.delete(doc.getReference());
                    }
                    // QUAN TRỌNG: Xóa trong feedback_notes (Lời nhắn của phụ huynh hiện ở máy GV)
                    return db.collection(AppConstants.COL_FEEDBACK_NOTES)
                            .whereEqualTo("childId", childId)
                            .get();
                })
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()) batch.delete(doc.getReference());
                    }
                    return db.collection(AppConstants.COL_CLASS_MEMBERS)
                            .whereEqualTo("childId", childId)
                            .get();
                })
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()) batch.delete(doc.getReference());
                    }
                    // Xóa cả trong bảng badges (nếu có)
                    return db.collection(AppConstants.COL_CHILD_BADGES)
                            .whereEqualTo("childId", childId)
                            .get();
                })
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()) batch.delete(doc.getReference());
                    }
                    return batch.commit();
                });
    }

    public Task<Void> softDeleteChildProfile(String childId) {
        return db.collection(AppConstants.COL_CHILD_PROFILES)
                .document(childId)
                .update("deletedAt", new java.util.Date());
    }

    // ==================== CHILD SETTINGS ====================

    public Task<DocumentSnapshot> getChildSettings(String childId) {
        return db.collection(AppConstants.COL_CHILD_PROFILES)
                .document(childId)
                .collection(AppConstants.SUBCOL_SETTINGS)
                .document(AppConstants.DOC_CHILD_SETTINGS)
                .get();
    }

    public Task<Void> saveChildSettings(String childId, ChildSettings settings) {
        return db.collection(AppConstants.COL_CHILD_PROFILES)
                .document(childId)
                .collection(AppConstants.SUBCOL_SETTINGS)
                .document(AppConstants.DOC_CHILD_SETTINGS)
                .set(settings);
    }

    // ==================== PARENT-CHILD LINKS ====================

    public Task<QuerySnapshot> getChildrenOfParent(String parentId) {
        return db.collection(AppConstants.COL_PARENT_CHILD_LINKS)
                .whereEqualTo("parentId", parentId)
                .get();
    }

    public Task<QuerySnapshot> getParentsOfChild(String childId) {
        return db.collection(AppConstants.COL_PARENT_CHILD_LINKS)
                .whereEqualTo("childId", childId)
                .get();
    }

    public Task<Void> createParentChildLink(String linkId, ParentChildLink link) {
        return db.collection(AppConstants.COL_PARENT_CHILD_LINKS)
                .document(linkId)
                .set(link);
    }

    // ==================== CHILD STATS ====================

    public Task<DocumentSnapshot> getChildStats(String childId) {
        return db.collection(AppConstants.COL_CHILD_STATS)
                .document(childId)
                .get();
    }

    public Task<Void> initChildStats(String childId) {
        ChildStats stats = new ChildStats(childId);
        return db.collection(AppConstants.COL_CHILD_STATS)
                .document(childId)
                .set(stats);
    }

    /** Cộng điểm cho bé và tự động kiểm tra tặng huy hiệu */
    public Task<Void> addPoints(String childId, int points) {
        return db.collection(AppConstants.COL_CHILD_STATS)
                .document(childId)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || !task.getResult().exists()) {
                        return db.collection(AppConstants.COL_CHILD_STATS)
                                .document(childId)
                                .update("totalPoints", FieldValue.increment(points), "lastActiveAt", new java.util.Date());
                    }
                    
                    Long currentStreak = task.getResult().getLong("streakDays");
                    if (currentStreak == null) currentStreak = 0L;
                    java.util.Date lastActive = task.getResult().getDate("lastActiveAt");
                    java.util.Date now = new java.util.Date();
                    
                    long newStreak = currentStreak;
                    if (lastActive != null) {
                        long diffMs = now.getTime() - lastActive.getTime();
                        long diffDays = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffMs);
                        
                        if (diffDays == 1 || diffDays == 0 && now.getDate() != lastActive.getDate()) {
                            newStreak = currentStreak + 1; // ngày tiếp theo
                        } else if (diffDays > 1) {
                            newStreak = 1; // Reset streak
                        } else {
                            // Cùng một ngày, không đổi
                        }
                    } else {
                        newStreak = 1;
                    }

                    return db.collection(AppConstants.COL_CHILD_STATS)
                            .document(childId)
                            .update(
                                "totalPoints", FieldValue.increment(points),
                                "lastActiveAt", now,
                                "streakDays", newStreak
                            );
                })
                .continueWithTask(task -> {
                    return db.collection(AppConstants.COL_CHILD_STATS).document(childId).get();
                })
                .continueWithTask(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        Long totalPoints = task.getResult().getLong("totalPoints");
                        if (totalPoints != null) {
                            checkAndAwardBadges(childId, totalPoints);
                        }
                    }
                    return Tasks.forResult(null);
                });
    }

    /** Ghi nhận hoàn thành 1 bài học cụ thể (từ 7 trò chơi học tập) */
    public void completeLesson(String childId, String lessonId) {
        db.collection(AppConstants.COL_CHILD_STATS)
                .document(childId)
                .update("completedLessons", FieldValue.arrayUnion(lessonId));
    }

    private void checkAndAwardBadges(String childId, long totalPoints) {
        // Mốc 500: Tân Binh Chăm Chỉ
        if (totalPoints >= 500) {
            awardBadgeIfMissing(childId, "badge_500", "Tân Binh Chăm Chỉ");
        }
        // Mốc 1000: Học Giả Nhí
        if (totalPoints >= 1000) {
            awardBadgeIfMissing(childId, "badge_1000", "Học Giả Nhí");
        }
        // Mốc 2000: Bậc Thầy Kiến Thức
        if (totalPoints >= 2000) {
            awardBadgeIfMissing(childId, "badge_2000", "Bậc Thầy Kiến Thức");
        }
        // Mốc 5000: Siêu Nhân Trí Tuệ
        if (totalPoints >= 5000) {
            awardBadgeIfMissing(childId, "badge_5000", "Siêu Nhân Trí Tuệ");
        }
        // Mốc 10000: Huyền Thoại KidLearn
        if (totalPoints >= 10000) {
            awardBadgeIfMissing(childId, "badge_10000", "Huyền Thoại KidLearn");
        }
    }

    private void awardBadgeIfMissing(String childId, String badgeId, String badgeName) {
        db.collection(AppConstants.COL_CHILD_BADGES)
                .whereEqualTo("childId", childId)
                .whereEqualTo("badgeId", badgeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Map<String, Object> badge = new HashMap<>();
                        badge.put("childId", childId);
                        badge.put("badgeId", badgeId);
                        badge.put("name", badgeName);
                        badge.put("awardedAt", new java.util.Date());
                        
                        db.collection(AppConstants.COL_CHILD_BADGES).add(badge);
                    }
                });
    }
}

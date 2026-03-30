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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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

    public Task<Void> softDeleteChildProfile(String childId) {
        return db.collection(AppConstants.COL_CHILD_PROFILES)
                .document(childId)
                .update("deletedAt", new java.util.Date());
    }

    // ==================== CHILD SETTINGS ====================

    /**
     * Lấy settings của bé.
     * Path: /child_profiles/{child_id}/settings/child_settings
     */
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

    /** Lấy tất cả con của một phụ huynh */
    public Task<QuerySnapshot> getChildrenOfParent(String parentId) {
        return db.collection(AppConstants.COL_PARENT_CHILD_LINKS)
                .whereEqualTo("parentId", parentId)
                .get();
    }

    /** Lấy tất cả phụ huynh của một bé */
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

    /** Cộng điểm cho bé */
    public Task<Void> addPoints(String childId, int points) {
        return db.collection(AppConstants.COL_CHILD_STATS)
                .document(childId)
                .update(
                    "totalPoints", com.google.firebase.firestore.FieldValue.increment(points),
                    "totalCompleted", com.google.firebase.firestore.FieldValue.increment(1),
                    "lastActiveAt", new java.util.Date()
                );
    }
}

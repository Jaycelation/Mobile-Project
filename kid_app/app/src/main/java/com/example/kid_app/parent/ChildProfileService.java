package com.example.kid_app.parent;

import com.example.kid_app.common.AppConstants;
import com.example.kid_app.data.model.ChildProfile;
import com.example.kid_app.data.model.ChildSettings;
import com.example.kid_app.data.model.ParentChildLink;
import com.example.kid_app.data.repository.ChildProfileRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.UUID;

/**
 * ChildProfileService — orchestration layer cho quản lý hồ sơ bé.
 *
 * Tách logic nghiệp vụ khỏi Activity:
 * - Tạo hồ sơ bé + link phụ huynh + khởi tạo stats trong một thao tác.
 * - Cập nhật hồ sơ bé.
 * - Quản lý child_settings.
 *
 * Lưu ý: Firestore không có transaction cross-collection hoàn hảo trên mobile,
 * nhưng dùng batched write để giảm rủi ro partial write.
 */
public class ChildProfileService {

    private final ChildProfileRepository childProfileRepository;

    public ChildProfileService() {
        this.childProfileRepository = new ChildProfileRepository();
    }

    // ==================== TẠO HỒ SƠ BÉ ====================

    /**
     * Tạo hồ sơ bé hoàn chỉnh:
     * 1. Sinh childId mới (UUID).
     * 2. Lưu /child_profiles/{childId}.
     * 3. Lưu /parent_child_links/{linkId}.
     * 4. Khởi tạo /child_stats/{childId}.
     * 5. Khởi tạo settings mặc định.
     *
     * Trả về childId nếu thành công (dùng để điều hướng sau khi tạo).
     */
    public Task<String> createChildProfile(String parentId, ChildProfile profile,
                                           String relationship) {
        String childId = UUID.randomUUID().toString();
        String linkId  = UUID.randomUUID().toString();

        // Bước 1 + 2: tạo profile và link song song bằng Tasks.whenAll
        Task<Void> createProfile = childProfileRepository.createChildProfile(childId, profile);

        ParentChildLink link = new ParentChildLink(parentId, childId, relationship, true);
        Task<Void> createLink = childProfileRepository.createParentChildLink(linkId, link);

        Task<Void> initStats    = childProfileRepository.initChildStats(childId);
        ChildSettings defaults  = defaultSettings(profile.getAgeGroup());
        Task<Void> initSettings = childProfileRepository.saveChildSettings(childId, defaults);

        return Tasks.whenAll(createProfile, createLink, initStats, initSettings)
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException() != null
                                ? task.getException()
                                : new Exception("Tạo hồ sơ bé thất bại");
                    }
                    return childId;
                });
    }

    // ==================== CẬP NHẬT HỒ SƠ BÉ ====================

    public Task<Void> updateChildProfile(String childId, ChildProfile profile) {
        return childProfileRepository.updateChildProfile(childId, profile);
    }

    // ==================== LẤY HỒ SƠ ====================

    public Task<DocumentSnapshot> getChildProfile(String childId) {
        return childProfileRepository.getChildProfile(childId);
    }

    /** Lấy tất cả link phụ huynh — UI sẽ dùng để load danh sách bé */
    public Task<QuerySnapshot> getChildrenLinksOfParent(String parentId) {
        return childProfileRepository.getChildrenOfParent(parentId);
    }

    // ==================== SETTINGS ====================

    public Task<DocumentSnapshot> getChildSettings(String childId) {
        return childProfileRepository.getChildSettings(childId);
    }

    public Task<Void> saveChildSettings(String childId, ChildSettings settings) {
        return childProfileRepository.saveChildSettings(childId, settings);
    }

    // ==================== HELPERS ====================

    /**
     * Trả về settings mặc định theo nhóm tuổi.
     * - Trẻ nhỏ (3-5): giới hạn 30 phút, AI tắt.
     * - Nhóm khác: 60 phút, AI bật.
     */
    private ChildSettings defaultSettings(String ageGroup) {
        if (AppConstants.AGE_GROUP_3_5.equals(ageGroup)) {
            return new ChildSettings(30, false, ageGroup != null ? ageGroup : AppConstants.AGE_GROUP_3_5);
        }
        String filter = ageGroup != null ? ageGroup : AppConstants.AGE_GROUP_6_8;
        return new ChildSettings(60, true, filter);
    }
}

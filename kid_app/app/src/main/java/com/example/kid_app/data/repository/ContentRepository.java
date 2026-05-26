package com.example.kid_app.data.repository;

import com.example.kid_app.common.AppConstants;
import com.example.kid_app.data.FirestoreHelper;
import com.example.kid_app.data.model.ContentCatalog;
import com.example.kid_app.data.model.ContentLevel;
import com.example.kid_app.data.model.QuizQuestion;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * ContentRepository — thao tác với:
 * - /content_catalog/
 * - /content_catalog/{id}/levels/
 * - /content_catalog/{id}/questions/
 */
public class ContentRepository {

    private final FirebaseFirestore db;

    public ContentRepository() {
        this.db = FirestoreHelper.getInstance().getFirestore();
    }

    // ==================== CONTENT CATALOG ====================

    // Chuc nang: lay tat ca noi dung hoc tap dang hoat dong.
    public Task<QuerySnapshot> getAllActiveContent() {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .whereEqualTo("status", AppConstants.STATUS_ACTIVE)
                .get(); // ❌ bỏ deletedAt
    }

    // Chuc nang: lay danh sach noi dung theo loai nhu game, quiz, mau sac hoac so dem.
    public Task<QuerySnapshot> getContentByType(String contentType) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .whereEqualTo("contentType", contentType)
                .whereEqualTo("status", AppConstants.STATUS_ACTIVE)
                .get(); // ❌ bỏ deletedAt
    }

    // Chuc nang: loc noi dung theo loai va nhom tuoi cua tre.
    public Task<QuerySnapshot> getContentByTypeAndAge(String contentType, String ageGroup) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .whereEqualTo("contentType", contentType)
                .whereEqualTo("ageGroup", ageGroup)
                .whereEqualTo("status", AppConstants.STATUS_ACTIVE)
                .get();
    }

    // Chuc nang: doc chi tiet mot muc noi dung theo id.
    public Task<DocumentSnapshot> getContentById(String contentId) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .get();
    }

    // Chuc nang: tao hoac ghi de mot muc noi dung hoc tap.
    public Task<Void> createContent(String contentId, ContentCatalog content) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .set(content);
    }

    // Chuc nang: an noi dung bang cach cap nhat trang thai deleted.
    public Task<Void> softDeleteContent(String contentId) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .update("status", AppConstants.STATUS_DELETED);
    }

    // ==================== CONTENT DETAIL ====================

    // Chuc nang: doc document chi tiet cua mot noi dung hoc tap.
    public Task<DocumentSnapshot> getContentDetail(String contentId, String detailDocId) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .collection("detail")
                .document(detailDocId)
                .get();
    }

    // Chuc nang: luu thong tin chi tiet cho mot noi dung hoc tap.
    public Task<Void> saveContentDetail(String contentId, String detailDocId, Object detail) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .collection("detail")
                .document(detailDocId)
                .set(detail);
    }

    // ==================== CONTENT LEVELS ====================

    // Chuc nang: lay danh sach level cua mot noi dung hoc tap.
    public Task<QuerySnapshot> getLevels(String contentId) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .collection(AppConstants.SUBCOL_CONTENT_LEVELS)
                .orderBy("levelNo")
                .get();
    }

    // Chuc nang: doc thong tin mot level cu the.
    public Task<DocumentSnapshot> getLevel(String contentId, String levelId) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .collection(AppConstants.SUBCOL_CONTENT_LEVELS)
                .document(levelId)
                .get();
    }

    // Chuc nang: luu level cho game hoac bai hoc.
    public Task<Void> saveLevel(String contentId, String levelId, ContentLevel level) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .collection(AppConstants.SUBCOL_CONTENT_LEVELS)
                .document(levelId)
                .set(level);
    }

    // ==================== QUIZ QUESTIONS ====================

    // Chuc nang: lay danh sach cau hoi cua mot quiz.
    public Task<QuerySnapshot> getQuizQuestions(String contentId) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .collection("questions")
                .get();
    }

    // Chuc nang: doc chi tiet mot cau hoi quiz.
    public Task<DocumentSnapshot> getQuestion(String contentId, String questionId) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .collection("questions")
                .document(questionId)
                .get();
    }

    // Chuc nang: luu cau hoi quiz vao Firestore.
    public Task<Void> saveQuestion(String contentId, String questionId, QuizQuestion question) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .collection("questions")
                .document(questionId)
                .set(question);
    }
}

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

    public Task<QuerySnapshot> getAllActiveContent() {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .whereEqualTo("status", AppConstants.STATUS_ACTIVE)
                .get(); // ❌ bỏ deletedAt
    }

    public Task<QuerySnapshot> getContentByType(String contentType) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .whereEqualTo("contentType", contentType)
                .whereEqualTo("status", AppConstants.STATUS_ACTIVE)
                .get(); // ❌ bỏ deletedAt
    }

    public Task<QuerySnapshot> getContentByTypeAndAge(String contentType, String ageGroup) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .whereEqualTo("contentType", contentType)
                .whereEqualTo("ageGroup", ageGroup)
                .whereEqualTo("status", AppConstants.STATUS_ACTIVE)
                .get();
    }

    public Task<DocumentSnapshot> getContentById(String contentId) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .get();
    }

    public Task<Void> createContent(String contentId, ContentCatalog content) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .set(content);
    }

    public Task<Void> softDeleteContent(String contentId) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .update("status", AppConstants.STATUS_DELETED);
    }

    // ==================== CONTENT DETAIL ====================

    public Task<DocumentSnapshot> getContentDetail(String contentId, String detailDocId) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .collection("detail")
                .document(detailDocId)
                .get();
    }

    public Task<Void> saveContentDetail(String contentId, String detailDocId, Object detail) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .collection("detail")
                .document(detailDocId)
                .set(detail);
    }

    // ==================== CONTENT LEVELS ====================

    public Task<QuerySnapshot> getLevels(String contentId) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .collection(AppConstants.SUBCOL_CONTENT_LEVELS)
                .orderBy("levelNo")
                .get();
    }

    public Task<DocumentSnapshot> getLevel(String contentId, String levelId) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .collection(AppConstants.SUBCOL_CONTENT_LEVELS)
                .document(levelId)
                .get();
    }

    public Task<Void> saveLevel(String contentId, String levelId, ContentLevel level) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .collection(AppConstants.SUBCOL_CONTENT_LEVELS)
                .document(levelId)
                .set(level);
    }

    // ==================== QUIZ QUESTIONS ====================

    public Task<QuerySnapshot> getQuizQuestions(String contentId) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .collection("questions")
                .get();
    }

    public Task<DocumentSnapshot> getQuestion(String contentId, String questionId) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .collection("questions")
                .document(questionId)
                .get();
    }

    public Task<Void> saveQuestion(String contentId, String questionId, QuizQuestion question) {
        return db.collection(AppConstants.COL_CONTENT_CATALOG)
                .document(contentId)
                .collection("questions")
                .document(questionId)
                .set(question);
    }
}
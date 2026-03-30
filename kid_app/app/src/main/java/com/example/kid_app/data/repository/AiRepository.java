package com.example.kid_app.data.repository;

import com.example.kid_app.common.AppConstants;
import com.example.kid_app.data.FirestoreHelper;
import com.example.kid_app.data.model.AiConversation;
import com.example.kid_app.data.model.AiMessage;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;

/**
 * AiRepository — thao tác với:
 * - /ai_conversations/{conversation_id}          (top-level)
 * - /ai_conversations/{conversation_id}/messages/{message_id}  (subcollection)
 *
 * Tại sao ai_conversations là top-level:
 * - Cần query "tất cả conversation của child X" không phụ thuộc document cha khác.
 * - Messages là subcollection vì chỉ cần đọc trong ngữ cảnh một conversation cụ thể.
 *
 * An toàn: repository không xử lý nội dung AI — chỉ lưu/đọc.
 * Logic gọi AI API nằm ở AiService (bước 10).
 */
public class AiRepository {

    private final FirebaseFirestore db;

    public AiRepository() {
        this.db = FirestoreHelper.getInstance().getFirestore();
    }

    // ==================== AI CONVERSATIONS ====================

    /**
     * Tạo conversation mới khi bé bắt đầu chat với AI.
     * Trả về DocumentReference để lấy conversationId.
     */
    public Task<DocumentReference> createConversation(AiConversation conversation) {
        return db.collection(AppConstants.COL_AI_CONVERSATIONS)
                .add(conversation);
    }

    /** Lấy conversation theo id */
    public Task<DocumentSnapshot> getConversation(String conversationId) {
        return db.collection(AppConstants.COL_AI_CONVERSATIONS)
                .document(conversationId)
                .get();
    }

    /**
     * Lấy tất cả conversation của bé, mới nhất trước.
     * Dùng để hiển thị lịch sử chat.
     */
    public Task<QuerySnapshot> getConversationsByChild(String childId) {
        return db.collection(AppConstants.COL_AI_CONVERSATIONS)
                .whereEqualTo("childId", childId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Lấy conversation theo context (ví dụ: quiz_help cho quiz cụ thể).
     * contextType: "free_chat" | "quiz_help" | "game_help"
     */
    public Task<QuerySnapshot> getConversationByContext(String childId,
                                                         String contextType,
                                                         String contextRefId) {
        return db.collection(AppConstants.COL_AI_CONVERSATIONS)
                .whereEqualTo("childId", childId)
                .whereEqualTo("contextType", contextType)
                .whereEqualTo("contextRefId", contextRefId)
                .limit(1)
                .get();
    }

    /** Cập nhật updatedAt khi có tin nhắn mới */
    public Task<Void> touchConversation(String conversationId) {
        return db.collection(AppConstants.COL_AI_CONVERSATIONS)
                .document(conversationId)
                .update("updatedAt", new Date());
    }

    // ==================== AI MESSAGES ====================

    /**
     * Lưu tin nhắn vào subcollection.
     * Path: /ai_conversations/{conversationId}/messages/{messageId}
     * Trả về DocumentReference để lấy messageId.
     */
    public Task<DocumentReference> addMessage(String conversationId, AiMessage message) {
        return db.collection(AppConstants.COL_AI_CONVERSATIONS)
                .document(conversationId)
                .collection(AppConstants.SUBCOL_AI_MESSAGES)
                .add(message);
    }

    /**
     * Lấy tất cả tin nhắn trong conversation, theo thứ tự thời gian.
     * Dùng để rebuild context khi mở lại cuộc trò chuyện.
     */
    public Task<QuerySnapshot> getMessages(String conversationId) {
        return db.collection(AppConstants.COL_AI_CONVERSATIONS)
                .document(conversationId)
                .collection(AppConstants.SUBCOL_AI_MESSAGES)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get();
    }

    /**
     * Lấy N tin nhắn gần nhất — dùng làm context window cho AI.
     * Giới hạn để tránh gửi quá nhiều token lên API.
     */
    public Task<QuerySnapshot> getRecentMessages(String conversationId, int limit) {
        return db.collection(AppConstants.COL_AI_CONVERSATIONS)
                .document(conversationId)
                .collection(AppConstants.SUBCOL_AI_MESSAGES)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
    }
}

package com.example.kid_app.data.mapper;

import android.util.Log;

import com.example.kid_app.data.model.Account;
import com.example.kid_app.data.model.ActivityAttempt;
import com.example.kid_app.data.model.AiConversation;
import com.example.kid_app.data.model.AiMessage;
import com.example.kid_app.data.model.AppClass;
import com.example.kid_app.data.model.Assignment;
import com.example.kid_app.data.model.AssignmentSubmission;
import com.example.kid_app.data.model.AttemptAnswer;
import com.example.kid_app.data.model.Badge;
import com.example.kid_app.data.model.ChildBadge;
import com.example.kid_app.data.model.ChildProfile;
import com.example.kid_app.data.model.ChildSettings;
import com.example.kid_app.data.model.ChildStats;
import com.example.kid_app.data.model.ClassMember;
import com.example.kid_app.data.model.ColorActivity;
import com.example.kid_app.data.model.ContentCatalog;
import com.example.kid_app.data.model.ContentLevel;
import com.example.kid_app.data.model.CountingActivity;
import com.example.kid_app.data.model.FeedbackNote;
import com.example.kid_app.data.model.Game;
import com.example.kid_app.data.model.LeaderboardSnapshot;
import com.example.kid_app.data.model.ParentChildLink;
import com.example.kid_app.data.model.Quiz;
import com.example.kid_app.data.model.QuizQuestion;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * DocumentMapper — chuyển đổi DocumentSnapshot ↔ Model Java.
 *
 * Firestore SDK hỗ trợ toModel(Class) tự động qua reflection,
 * nhưng mapper tường minh này giúp:
 * 1. Xử lý null-safety rõ ràng thay vì crash silently.
 * 2. Dễ debug khi field name không khớp (camelCase Firestore vs Java field).
 * 3. Dễ thêm logic transform (ví dụ: Date → String hiển thị) nếu cần.
 *
 * Pattern: mỗi toXxx(DocumentSnapshot) method:
 *   - Trả null nếu document không tồn tại.
 *   - Dùng toObject() của Firestore SDK (đã handle @DocumentId và type conv).
 *   - Log warning nếu có lỗi để dễ debug.
 *
 * Pattern: mỗi listXxx(QuerySnapshot) method:
 *   - Trả list rỗng (không phải null) nếu query không có kết quả.
 */
public final class DocumentMapper {

    private static final String TAG = "DocumentMapper";

    private DocumentMapper() {}

    // ==================== ACCOUNT ====================

    public static Account toAccount(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(Account.class);
        } catch (Exception e) {
            Log.w(TAG, "toAccount failed: " + e.getMessage());
            return null;
        }
    }

    public static List<Account> listAccounts(QuerySnapshot snap) {
        List<Account> list = new ArrayList<>();
        if (snap == null) return list;
        for (DocumentSnapshot doc : snap.getDocuments()) {
            Account a = toAccount(doc);
            if (a != null) list.add(a);
        }
        return list;
    }

    // ==================== CHILD PROFILE ====================

    public static ChildProfile toChildProfile(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(ChildProfile.class);
        } catch (Exception e) {
            Log.w(TAG, "toChildProfile failed: " + e.getMessage());
            return null;
        }
    }

    public static List<ChildProfile> listChildProfiles(QuerySnapshot snap) {
        List<ChildProfile> list = new ArrayList<>();
        if (snap == null) return list;
        for (DocumentSnapshot doc : snap.getDocuments()) {
            ChildProfile p = toChildProfile(doc);
            if (p != null) list.add(p);
        }
        return list;
    }

    // ==================== CHILD SETTINGS ====================

    public static ChildSettings toChildSettings(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(ChildSettings.class);
        } catch (Exception e) {
            Log.w(TAG, "toChildSettings failed: " + e.getMessage());
            return null;
        }
    }

    // ==================== CHILD STATS ====================

    public static ChildStats toChildStats(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(ChildStats.class);
        } catch (Exception e) {
            Log.w(TAG, "toChildStats failed: " + e.getMessage());
            return null;
        }
    }

    // ==================== PARENT-CHILD LINK ====================

    public static ParentChildLink toParentChildLink(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(ParentChildLink.class);
        } catch (Exception e) {
            Log.w(TAG, "toParentChildLink failed: " + e.getMessage());
            return null;
        }
    }

    public static List<ParentChildLink> listParentChildLinks(QuerySnapshot snap) {
        List<ParentChildLink> list = new ArrayList<>();
        if (snap == null) return list;
        for (DocumentSnapshot doc : snap.getDocuments()) {
            ParentChildLink l = toParentChildLink(doc);
            if (l != null) list.add(l);
        }
        return list;
    }

    // ==================== CONTENT CATALOG ====================

    public static ContentCatalog toContentCatalog(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(ContentCatalog.class);
        } catch (Exception e) {
            Log.w(TAG, "toContentCatalog failed: " + e.getMessage());
            return null;
        }
    }

    public static List<ContentCatalog> listContentCatalog(QuerySnapshot snap) {
        List<ContentCatalog> list = new ArrayList<>();
        if (snap == null) return list;
        for (DocumentSnapshot doc : snap.getDocuments()) {
            ContentCatalog c = toContentCatalog(doc);
            if (c != null) list.add(c);
        }
        return list;
    }

    // ==================== CONTENT LEVEL ====================

    public static ContentLevel toContentLevel(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(ContentLevel.class);
        } catch (Exception e) {
            Log.w(TAG, "toContentLevel failed: " + e.getMessage());
            return null;
        }
    }

    public static List<ContentLevel> listContentLevels(QuerySnapshot snap) {
        List<ContentLevel> list = new ArrayList<>();
        if (snap == null) return list;
        for (DocumentSnapshot doc : snap.getDocuments()) {
            ContentLevel l = toContentLevel(doc);
            if (l != null) list.add(l);
        }
        return list;
    }

    // ==================== GAME ====================

    public static Game toGame(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(Game.class);
        } catch (Exception e) {
            Log.w(TAG, "toGame failed: " + e.getMessage());
            return null;
        }
    }

    // ==================== QUIZ + QUESTIONS ====================

    public static Quiz toQuiz(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(Quiz.class);
        } catch (Exception e) {
            Log.w(TAG, "toQuiz failed: " + e.getMessage());
            return null;
        }
    }

    public static QuizQuestion toQuizQuestion(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(QuizQuestion.class);
        } catch (Exception e) {
            Log.w(TAG, "toQuizQuestion failed: " + e.getMessage());
            return null;
        }
    }

    public static List<QuizQuestion> listQuizQuestions(QuerySnapshot snap) {
        List<QuizQuestion> list = new ArrayList<>();
        if (snap == null) return list;
        for (DocumentSnapshot doc : snap.getDocuments()) {
            QuizQuestion q = toQuizQuestion(doc);
            if (q != null) list.add(q);
        }
        return list;
    }

    // ==================== COLOR ACTIVITY ====================

    public static ColorActivity toColorActivity(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(ColorActivity.class);
        } catch (Exception e) {
            Log.w(TAG, "toColorActivity failed: " + e.getMessage());
            return null;
        }
    }

    // ==================== COUNTING ACTIVITY ====================

    public static CountingActivity toCountingActivity(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(CountingActivity.class);
        } catch (Exception e) {
            Log.w(TAG, "toCountingActivity failed: " + e.getMessage());
            return null;
        }
    }

    // ==================== ACTIVITY ATTEMPT + ANSWERS ====================

    public static ActivityAttempt toActivityAttempt(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(ActivityAttempt.class);
        } catch (Exception e) {
            Log.w(TAG, "toActivityAttempt failed: " + e.getMessage());
            return null;
        }
    }

    public static List<ActivityAttempt> listActivityAttempts(QuerySnapshot snap) {
        List<ActivityAttempt> list = new ArrayList<>();
        if (snap == null) return list;
        for (DocumentSnapshot doc : snap.getDocuments()) {
            ActivityAttempt a = toActivityAttempt(doc);
            if (a != null) list.add(a);
        }
        return list;
    }

    public static AttemptAnswer toAttemptAnswer(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(AttemptAnswer.class);
        } catch (Exception e) {
            Log.w(TAG, "toAttemptAnswer failed: " + e.getMessage());
            return null;
        }
    }

    public static List<AttemptAnswer> listAttemptAnswers(QuerySnapshot snap) {
        List<AttemptAnswer> list = new ArrayList<>();
        if (snap == null) return list;
        for (DocumentSnapshot doc : snap.getDocuments()) {
            AttemptAnswer a = toAttemptAnswer(doc);
            if (a != null) list.add(a);
        }
        return list;
    }

    // ==================== CLASS + MEMBERS ====================

    public static AppClass toAppClass(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(AppClass.class);
        } catch (Exception e) {
            Log.w(TAG, "toAppClass failed: " + e.getMessage());
            return null;
        }
    }

    public static List<AppClass> listAppClasses(QuerySnapshot snap) {
        List<AppClass> list = new ArrayList<>();
        if (snap == null) return list;
        for (DocumentSnapshot doc : snap.getDocuments()) {
            AppClass c = toAppClass(doc);
            if (c != null) list.add(c);
        }
        return list;
    }

    public static ClassMember toClassMember(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(ClassMember.class);
        } catch (Exception e) {
            Log.w(TAG, "toClassMember failed: " + e.getMessage());
            return null;
        }
    }

    public static List<ClassMember> listClassMembers(QuerySnapshot snap) {
        List<ClassMember> list = new ArrayList<>();
        if (snap == null) return list;
        for (DocumentSnapshot doc : snap.getDocuments()) {
            ClassMember m = toClassMember(doc);
            if (m != null) list.add(m);
        }
        return list;
    }

    // ==================== ASSIGNMENT + SUBMISSIONS ====================

    public static Assignment toAssignment(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(Assignment.class);
        } catch (Exception e) {
            Log.w(TAG, "toAssignment failed: " + e.getMessage());
            return null;
        }
    }

    public static List<Assignment> listAssignments(QuerySnapshot snap) {
        List<Assignment> list = new ArrayList<>();
        if (snap == null) return list;
        for (DocumentSnapshot doc : snap.getDocuments()) {
            Assignment a = toAssignment(doc);
            if (a != null) list.add(a);
        }
        return list;
    }

    public static AssignmentSubmission toAssignmentSubmission(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(AssignmentSubmission.class);
        } catch (Exception e) {
            Log.w(TAG, "toAssignmentSubmission failed: " + e.getMessage());
            return null;
        }
    }

    public static List<AssignmentSubmission> listAssignmentSubmissions(QuerySnapshot snap) {
        List<AssignmentSubmission> list = new ArrayList<>();
        if (snap == null) return list;
        for (DocumentSnapshot doc : snap.getDocuments()) {
            AssignmentSubmission s = toAssignmentSubmission(doc);
            if (s != null) list.add(s);
        }
        return list;
    }

    // ==================== BADGE + CHILD BADGE ====================

    public static Badge toBadge(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(Badge.class);
        } catch (Exception e) {
            Log.w(TAG, "toBadge failed: " + e.getMessage());
            return null;
        }
    }

    public static List<Badge> listBadges(QuerySnapshot snap) {
        List<Badge> list = new ArrayList<>();
        if (snap == null) return list;
        for (DocumentSnapshot doc : snap.getDocuments()) {
            Badge b = toBadge(doc);
            if (b != null) list.add(b);
        }
        return list;
    }

    public static ChildBadge toChildBadge(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(ChildBadge.class);
        } catch (Exception e) {
            Log.w(TAG, "toChildBadge failed: " + e.getMessage());
            return null;
        }
    }

    public static List<ChildBadge> listChildBadges(QuerySnapshot snap) {
        List<ChildBadge> list = new ArrayList<>();
        if (snap == null) return list;
        for (DocumentSnapshot doc : snap.getDocuments()) {
            ChildBadge cb = toChildBadge(doc);
            if (cb != null) list.add(cb);
        }
        return list;
    }

    // ==================== FEEDBACK + LEADERBOARD ====================

    public static FeedbackNote toFeedbackNote(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(FeedbackNote.class);
        } catch (Exception e) {
            Log.w(TAG, "toFeedbackNote failed: " + e.getMessage());
            return null;
        }
    }

    public static List<FeedbackNote> listFeedbackNotes(QuerySnapshot snap) {
        List<FeedbackNote> list = new ArrayList<>();
        if (snap == null) return list;
        for (DocumentSnapshot doc : snap.getDocuments()) {
            FeedbackNote n = toFeedbackNote(doc);
            if (n != null) list.add(n);
        }
        return list;
    }

    public static LeaderboardSnapshot toLeaderboardSnapshot(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(LeaderboardSnapshot.class);
        } catch (Exception e) {
            Log.w(TAG, "toLeaderboardSnapshot failed: " + e.getMessage());
            return null;
        }
    }

    // ==================== AI CONVERSATION + MESSAGES ====================

    public static AiConversation toAiConversation(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(AiConversation.class);
        } catch (Exception e) {
            Log.w(TAG, "toAiConversation failed: " + e.getMessage());
            return null;
        }
    }

    public static List<AiConversation> listAiConversations(QuerySnapshot snap) {
        List<AiConversation> list = new ArrayList<>();
        if (snap == null) return list;
        for (DocumentSnapshot doc : snap.getDocuments()) {
            AiConversation c = toAiConversation(doc);
            if (c != null) list.add(c);
        }
        return list;
    }

    public static AiMessage toAiMessage(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            return doc.toObject(AiMessage.class);
        } catch (Exception e) {
            Log.w(TAG, "toAiMessage failed: " + e.getMessage());
            return null;
        }
    }

    public static List<AiMessage> listAiMessages(QuerySnapshot snap) {
        List<AiMessage> list = new ArrayList<>();
        if (snap == null) return list;
        for (DocumentSnapshot doc : snap.getDocuments()) {
            AiMessage m = toAiMessage(doc);
            if (m != null) list.add(m);
        }
        return list;
    }
}

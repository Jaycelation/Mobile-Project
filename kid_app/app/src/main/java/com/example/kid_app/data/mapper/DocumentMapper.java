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
 */
public final class DocumentMapper {

    private static final String TAG = "DocumentMapper";

    private DocumentMapper() {}

    // ==================== ACCOUNT ====================

    public static Account toAccount(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try {
            Account a = doc.toObject(Account.class);
            if (a != null) {
                if (a.getFullName() == null) a.setFullName(doc.getString("fullName"));
                if (a.getEmail() == null) a.setEmail(doc.getString("email"));
                if (a.getRole() == null) a.setRole(doc.getString("role"));
                a.setAccountId(doc.getId());
            }
            return a;
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
        try { return doc.toObject(ChildSettings.class); } catch (Exception e) { return null; }
    }

    // ==================== CHILD STATS ====================

    public static ChildStats toChildStats(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try { return doc.toObject(ChildStats.class); } catch (Exception e) { return null; }
    }

    // ==================== PARENT-CHILD LINK ====================

    public static ParentChildLink toParentChildLink(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try { return doc.toObject(ParentChildLink.class); } catch (Exception e) { return null; }
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
            ContentCatalog c = doc.toObject(ContentCatalog.class);
            if (c != null) c.setContentId(doc.getId());
            return c;
        } catch (Exception e) { return null; }
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

    // ==================== QUIZ QUESTION ====================

    public static QuizQuestion toQuizQuestion(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try { return doc.toObject(QuizQuestion.class); } catch (Exception e) { return null; }
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

    // ==================== ACTIVITY ATTEMPT ====================

    public static ActivityAttempt toActivityAttempt(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try { return doc.toObject(ActivityAttempt.class); } catch (Exception e) { return null; }
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

    // ==================== FEEDBACK ====================

    public static FeedbackNote toFeedbackNote(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try { return doc.toObject(FeedbackNote.class); } catch (Exception e) { return null; }
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

    // ==================== CHILD BADGE ====================

    public static ChildBadge toChildBadge(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return null;
        try { return doc.toObject(ChildBadge.class); } catch (Exception e) { return null; }
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
}

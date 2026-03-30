package com.example.kid_app.common;

/**
 * Hằng số toàn cục của ứng dụng.
 * Chứa: key Intent, SharedPreferences, collection Firestore, role người dùng.
 */
public final class AppConstants {

    private AppConstants() {}

    // ==================== USER ROLES ====================
    public static final String ROLE_PARENT = "parent";
    public static final String ROLE_TEACHER = "teacher";
    public static final String ROLE_ADMIN = "admin";
    // Child không đăng nhập trực tiếp — được truy cập qua profile của Parent

    // ==================== INTENT KEYS ====================
    public static final String KEY_CHILD_ID = "child_id";
    public static final String KEY_CONTENT_ID = "content_id";
    public static final String KEY_LEVEL_ID = "level_id";
    public static final String KEY_CLASS_ID = "class_id";
    public static final String KEY_QUIZ_ID = "quiz_id";
    public static final String KEY_ASSIGNMENT_ID = "assignment_id";
    public static final String KEY_CONVERSATION_ID = "conversation_id";

    // ==================== SHARED PREFERENCES ====================
    public static final String PREF_NAME = "kidlearn_prefs";
    public static final String PREF_SELECTED_CHILD_ID = "selected_child_id";
    public static final String PREF_USER_ROLE = "user_role";

    // ==================== FIRESTORE COLLECTIONS (Top-level) ====================
    // Giải thích: các collection này là top-level vì cần query độc lập
    // và không phụ thuộc chặt vào một document cha cụ thể

    /** Tài khoản người dùng (parent, teacher, admin) */
    public static final String COL_ACCOUNTS = "accounts";

    /** Liên kết parent - child */
    public static final String COL_PARENT_CHILD_LINKS = "parent_child_links";

    /** Hồ sơ trẻ em */
    public static final String COL_CHILD_PROFILES = "child_profiles";

    /** Danh mục nội dung học (game, quiz, màu sắc, số đếm) */
    public static final String COL_CONTENT_CATALOG = "content_catalog";

    /** Lớp học */
    public static final String COL_CLASSES = "classes";

    /** Thành viên lớp học */
    public static final String COL_CLASS_MEMBERS = "class_members";

    /** Bài tập giáo viên giao */
    public static final String COL_ASSIGNMENTS = "assignments";

    /** Bài nộp của học sinh */
    public static final String COL_ASSIGNMENT_SUBMISSIONS = "assignment_submissions";

    /** Huy hiệu (định nghĩa, toàn hệ thống) */
    public static final String COL_BADGES = "badges";

    /** Huy hiệu của từng bé */
    public static final String COL_CHILD_BADGES = "child_badges";

    /** Phản hồi từ giáo viên */
    public static final String COL_FEEDBACK_NOTES = "feedback_notes";

    /** Thống kê tổng hợp của bé */
    public static final String COL_CHILD_STATS = "child_stats";

    /** Bảng xếp hạng snapshot */
    public static final String COL_LEADERBOARD_SNAPSHOTS = "leaderboard_snapshots";

    /** Lịch sử chat AI */
    public static final String COL_AI_CONVERSATIONS = "ai_conversations";

    // ==================== FIRESTORE SUBCOLLECTIONS ====================
    // Giải thích: subcollection khi dữ liệu chỉ có nghĩa gắn với document cha

    /** Cài đặt của bé — subcollection dưới child_profiles/<child_id>/settings */
    public static final String SUBCOL_SETTINGS = "settings";
    public static final String DOC_CHILD_SETTINGS = "child_settings";

    /** Levels của content — subcollection dưới content_catalog/<content_id>/levels */
    public static final String SUBCOL_CONTENT_LEVELS = "levels";

    /** Lần làm bài — subcollection dưới child_profiles/<child_id>/attempts */
    public static final String SUBCOL_ACTIVITY_ATTEMPTS = "activity_attempts";

    /** Câu trả lời — subcollection dưới attempts/<attempt_id>/answers */
    public static final String SUBCOL_ATTEMPT_ANSWERS = "answers";

    /** Tin nhắn AI — subcollection dưới ai_conversations/<conv_id>/messages */
    public static final String SUBCOL_AI_MESSAGES = "messages";

    // ==================== CONTENT TYPES ====================
    public static final String CONTENT_TYPE_GAME = "game";
    public static final String CONTENT_TYPE_QUIZ = "quiz";
    public static final String CONTENT_TYPE_COLOR = "color_activity";
    public static final String CONTENT_TYPE_COUNTING = "counting_activity";

    // ==================== SESSION TYPES ====================
    public static final String SESSION_FREE_PLAY = "free_play";
    public static final String SESSION_ASSIGNMENT = "assignment";

    // ==================== AGE GROUPS ====================
    public static final String AGE_GROUP_3_5 = "3-5";
    public static final String AGE_GROUP_6_8 = "6-8";
    public static final String AGE_GROUP_9_12 = "9-12";

    // ==================== STATUS ====================
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_INACTIVE = "inactive";
    public static final String STATUS_DELETED = "deleted";

    // ==================== DIFFICULTY ====================
    public static final String DIFFICULTY_EASY = "easy";
    public static final String DIFFICULTY_MEDIUM = "medium";
    public static final String DIFFICULTY_HARD = "hard";

    // ==================== AI ====================
    /** Sender role trong ai_messages: 0=user(child), 1=assistant(AI) */
    public static final int AI_ROLE_USER = 0;
    public static final int AI_ROLE_ASSISTANT = 1;
    public static final String AI_CONTEXT_FREE = "free_chat";
    public static final String AI_CONTEXT_QUIZ = "quiz_help";
    public static final String AI_CONTEXT_GAME = "game_help";
}

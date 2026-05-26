package com.example.kid_app.child;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.text.SimpleDateFormat;

public class ChildHomeActivity extends AppCompatActivity {

    private TextView tvChildGreeting, tvTodayDate, tvCurrentBadge;
    private TextView tvTotalPoints, tvStreak, tvCompleted;
    private TextView tvAssignmentCount;
    private ImageView ivChildAvatar;
    private CardView cardFeedback, cardAssignments; 

    private CardView cardLearningPortal, cardPracticePortal;
    private CardView fabAiChat;
    private LinearLayout navHome, navCommunity, navProgress, navProfile;

    private ImageButton btnBack;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String selectedChildId;
    private String childClassId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_child_home);

        // Chuc nang: khoi tao Firebase Auth de lay phien dang nhap hien tai.
        mAuth = FirebaseAuth.getInstance();
        // Chuc nang: khoi tao Firestore de doc ghi du lieu cloud cho man hinh.
        db = FirebaseFirestore.getInstance();

        SharedPreferences prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        selectedChildId = prefs.getString(AppConstants.PREF_SELECTED_CHILD_ID, null);

        initViews();
        setupStaticUI();
        setupClickEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (selectedChildId != null) {
            loadChildInfo();
            loadStats();
        } else {
            Toast.makeText(this, "Không tìm thấy hồ sơ bé. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        tvChildGreeting = findViewById(R.id.tv_child_greeting);
        tvTodayDate = findViewById(R.id.tv_today_date);
        tvCurrentBadge = findViewById(R.id.tv_current_badge);
        ivChildAvatar = findViewById(R.id.iv_child_avatar);

        tvTotalPoints = findViewById(R.id.tv_total_points);
        tvStreak = findViewById(R.id.tv_streak);
        tvCompleted = findViewById(R.id.tv_completed);
        tvAssignmentCount = findViewById(R.id.tv_assignment_count);

        cardFeedback = findViewById(R.id.card_feedback);
        cardAssignments = findViewById(R.id.card_assignments);

        cardLearningPortal = findViewById(R.id.card_learning_portal);
        cardPracticePortal = findViewById(R.id.card_practice_portal);
        fabAiChat = findViewById(R.id.fab_ai_chat);

        navHome = findViewById(R.id.nav_home);
        navCommunity = findViewById(R.id.nav_community);
        navProgress = findViewById(R.id.nav_progress);
        navProfile = findViewById(R.id.nav_profile);

        btnBack = findViewById(R.id.btn_back);
    }

    private void setupStaticUI() {
        String today = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN")).format(new Date());
        tvTodayDate.setText(today);
    }

    private void setupClickEvents() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        
        if (cardFeedback != null) cardFeedback.setOnClickListener(v -> {
            if (isJoinedClass()) {
                Intent intent = new Intent(this, ParentFeedbackActivity.class);
                startActivity(intent);
            } else {
                showJoinClassNotice();
            }
        });

        if (cardAssignments != null) cardAssignments.setOnClickListener(v -> {
            if (isJoinedClass()) {
                Intent intent = new Intent(this, ChildAssignmentActivity.class);
                intent.putExtra("classId", childClassId);
                startActivity(intent);
            } else {
                showJoinClassNotice();
            }
        });

        if (cardLearningPortal != null) cardLearningPortal.setOnClickListener(v -> 
                startActivity(new Intent(this, LearningListActivity.class)));

        if (cardPracticePortal != null) cardPracticePortal.setOnClickListener(v -> 
                startActivity(new Intent(this, GameListActivity.class)));

        if (fabAiChat != null) fabAiChat.setOnClickListener(v -> 
                startActivity(new Intent(this, AiChatActivity.class)));

        if (navCommunity != null) navCommunity.setOnClickListener(v -> {
            if (isJoinedClass()) {
                startActivity(new Intent(this, LeaderboardActivity.class));
            } else {
                showJoinClassNotice();
            }
        });

        if (navProgress != null) navProgress.setOnClickListener(v -> 
                startActivity(new Intent(this, ChildProgressActivity.class)));

        if (navProfile != null) navProfile.setOnClickListener(v -> 
                startActivity(new Intent(this, ChildProfileActivity.class)));
    }

    private boolean isJoinedClass() {
        return childClassId != null && !childClassId.isEmpty();
    }

    private void showJoinClassNotice() {
        Toast.makeText(this, "Tính năng này yêu cầu bé tham gia lớp học trước nhé!", Toast.LENGTH_LONG).show();
    }

    private void loadChildInfo() {
        // Chuc nang: goi Firestore de doc hoac ghi du lieu cho chuc nang hien tai.
        db.collection(AppConstants.COL_CHILD_PROFILES)
                .document(selectedChildId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name == null || name.isEmpty()) name = doc.getString("fullName");
                        if (name == null || name.isEmpty()) name = "bé";
                        tvChildGreeting.setText("Chào " + name + "! 👋");

                        String gender = doc.getString("gender");
                        if ("Nữ".equals(gender) || "Female".equals(gender)) {
                            ivChildAvatar.setImageResource(R.drawable.hoc_sinh_nu);
                        } else {
                            ivChildAvatar.setImageResource(R.drawable.hoc_sinh_nam);
                        }
                        
                        childClassId = doc.getString("currentClassId");
                        if (childClassId == null || childClassId.isEmpty()) {
                            childClassId = doc.getString("classId");
                        }
                        
                        if (isJoinedClass()) {
                            loadAssignments();
                        } else {
                            tvAssignmentCount.setText("Bé chưa tham gia lớp học nào.");
                            tvAssignmentCount.setTextColor(getResources().getColor(R.color.text_secondary));
                        }
                    }
                });
    }

    private void loadAssignments() {
        if (childClassId == null || selectedChildId == null) return;

        // Chuc nang: goi Firestore de doc hoac ghi du lieu cho chuc nang hien tai.
        db.collection(AppConstants.COL_ASSIGNMENT_SUBMISSIONS)
                .whereEqualTo("childId", selectedChildId)
                .whereEqualTo("status", "submitted")
                .get()
                .addOnSuccessListener(submissionSnaps -> {
                    Set<String> submittedIds = new HashSet<>();
                    for (QueryDocumentSnapshot doc : submissionSnaps) {
                        submittedIds.add(doc.getString("assignmentId"));
                    }

                    // Chuc nang: goi Firestore de doc hoac ghi du lieu cho chuc nang hien tai.
                    db.collection("assignments")
                            .whereEqualTo("classId", childClassId)
                            .whereEqualTo("status", "active")
                            .get()
                            .addOnSuccessListener(assignmentSnaps -> {
                                int count = 0;
                                for (QueryDocumentSnapshot doc : assignmentSnaps) {
                                    if (!submittedIds.contains(doc.getId())) {
                                        count++;
                                    }
                                }
                                
                                if (count > 0) {
                                    tvAssignmentCount.setText("Bé có " + count + " bài tập mới đang chờ!");
                                    tvAssignmentCount.setTextColor(getResources().getColor(R.color.secondary_orange));
                                } else {
                                    tvAssignmentCount.setText("Hiện không có bài tập nào.");
                                    tvAssignmentCount.setTextColor(getResources().getColor(R.color.text_secondary));
                                }
                            });
                });
    }

    private void loadStats() {
        // Chuc nang: goi Firestore de doc hoac ghi du lieu cho chuc nang hien tai.
        db.collection(AppConstants.COL_CHILD_STATS)
                .document(selectedChildId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        long points = getSafeLong(doc, "totalPoints");
                        long streak = getSafeLong(doc, "streakDays");
                        
                        List<?> completedLessons = (List<?>) doc.get("completedLessons");
                        long completedCount = (completedLessons != null) ? completedLessons.size() : 0;

                        tvTotalPoints.setText(String.valueOf(points));
                        tvStreak.setText(String.valueOf(streak));
                        tvCompleted.setText(String.valueOf(completedCount));
                        
                        updateBadgeText(points);
                    } else {
                        tvTotalPoints.setText("0");
                        tvStreak.setText("0");
                        tvCompleted.setText("0");
                        updateBadgeText(0);
                    }
                });
    }

    private void updateBadgeText(long points) {
        String badge = "Học viên mới 🌱";
        if (points >= 10000) badge = "Huyền Thoại KidLearn 👑";
        else if (points >= 5000) badge = "Siêu Nhân Trí Tuệ 🚀";
        else if (points >= 2000) badge = "Bậc Thầy Kiến Thức 🎓";
        else if (points >= 1000) badge = "Học Giả Nhí 📚";
        else if (points >= 500) badge = "Tân Binh Chăm Chỉ 🎖️";
        else if (points >= 20) badge = "Mầm Non Chăm Chỉ 🌱";
        
        if (tvCurrentBadge != null) {
            tvCurrentBadge.setText("Danh hiệu: " + badge);
        }
    }

    private long getSafeLong(DocumentSnapshot doc, String key) {
        Object value = doc.get(key);
        if (value instanceof Number) return ((Number) value).longValue();
        return 0;
    }
}

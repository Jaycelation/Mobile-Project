package com.example.kid_app.child;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.example.kid_app.R;
import com.example.kid_app.auth.AuthService;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.mapper.DocumentMapper;
import com.example.kid_app.data.model.ChildProfile;
import com.example.kid_app.data.model.ChildSettings;
import com.example.kid_app.data.model.ChildStats;
import com.example.kid_app.data.repository.ChildProfileRepository;
import com.example.kid_app.parent.ParentHomeActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * ChildHomeActivity — Trang chủ của Bé (Bước 5).
 *
 * Chức năng:
 * 1. Đọc selectedChildId từ SharedPreferences (đặt bởi ParentHomeActivity).
 * 2. Load ChildProfile để hiển thị tên/avatar bé.
 * 3. Load ChildStats để hiển thị điểm / streak / số bài hoàn thành.
 * 4. Load ChildSettings để kiểm tra ai_enabled.
 * 5. Điều hướng sang 4 module placeholder + AI Chat.
 * 6. Nút "chuyển bé" → quay về ParentHomeActivity để đổi bé.
 *
 * Lưu ý thiết kế:
 * - Child KHÔNG đăng nhập trực tiếp; Firebase Auth vẫn đang login với tài khoản Parent.
 * - childId lấy từ SharedPreferences được ghi bởi Parent khi chọn bé.
 * - ChildSettings.ai_enabled kiểm soát AI Chat có khả dụng không.
 */
public class ChildHomeActivity extends BaseActivity {

    private ChildProfileRepository childProfileRepository;
    private AuthService authService;

    // Views
    private TextView tvGreeting;
    private TextView tvDate;
    private TextView tvTotalPoints;
    private TextView tvStreak;
    private TextView tvCompleted;
    private TextView tvAiStatus;
    private CardView cardAiAssistant;

    // State
    private String childId;
    private boolean aiEnabled = true; // mặc định bật, sẽ đọc từ settings

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_home);

        childProfileRepository = new ChildProfileRepository();
        authService = new AuthService();

        // Đọc childId từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        childId = prefs.getString(AppConstants.PREF_SELECTED_CHILD_ID, null);

        if (childId == null) {
            // Không có bé nào được chọn → quay về màn Parent chọn lại
            showToast("Vui lòng chọn hồ sơ bé trước!");
            navigateToClearStack(ParentHomeActivity.class);
            return;
        }

        bindViews();
        setTodayDate();
        loadChildData();
    }

    // ==================== BIND VIEWS ====================

    private void bindViews() {
        tvGreeting     = findViewById(R.id.tv_child_greeting);
        tvDate         = findViewById(R.id.tv_today_date);
        tvTotalPoints  = findViewById(R.id.tv_total_points);
        tvStreak       = findViewById(R.id.tv_streak);
        tvCompleted    = findViewById(R.id.tv_completed);
        tvAiStatus     = findViewById(R.id.tv_ai_status);
        cardAiAssistant = findViewById(R.id.card_ai_assistant);

        // Module cards
        CardView cardGames    = findViewById(R.id.card_games);
        CardView cardQuiz     = findViewById(R.id.card_quiz);
        CardView cardColors   = findViewById(R.id.card_colors);
        CardView cardCounting = findViewById(R.id.card_counting);

        cardGames.setOnClickListener(v -> openModule(
                GameListActivity.class, "🎮", "Trò chơi giáo dục", "Bước 6"));

        cardQuiz.setOnClickListener(v -> openModule(
                QuizListActivity.class, "🧩", "Câu đố & Trắc nghiệm", "Bước 7"));

        cardColors.setOnClickListener(v -> openModule(
                ColorListActivity.class, "🎨", "Học màu sắc", "Bước 8"));

        cardCounting.setOnClickListener(v -> openModule(
                CountingListActivity.class, "🔢", "Học số đếm", "Bước 9"));

        cardAiAssistant.setOnClickListener(v -> openAiChat());

        // Nút chuyển bé / quay về parent
        findViewById(R.id.btn_switch_child).setOnClickListener(v ->
                navigateToClearStack(ParentHomeActivity.class));

        // Nút Settings → mở ChildSettingsActivity của phụ huynh (đọc childId)
        findViewById(R.id.btn_settings).setOnClickListener(v -> openParentSettings());
    }

    // ==================== LOAD DATA ====================

    /**
     * Load dữ liệu bé: profile → greeting và stats → số liệu.
     * Load song song settings → kiểm tra AI.
     */
    private void loadChildData() {
        // Load profile
        childProfileRepository.getChildProfile(childId)
                .addOnSuccessListener(doc -> {
                    ChildProfile profile = DocumentMapper.toChildProfile(doc);
                    if (profile != null) {
                        updateGreeting(profile);
                    }
                })
                .addOnFailureListener(e -> {
                    tvGreeting.setText(getString(R.string.child_home_greeting, "Bé yêu"));
                });

        // Load stats
        childProfileRepository.getChildStats(childId)
                .addOnSuccessListener(doc -> {
                    ChildStats stats = DocumentMapper.toChildStats(doc);
                    if (stats != null) {
                        updateStats(stats);
                    } else {
                        // Stats chưa tồn tại → hiển thị 0
                        updateStats(new ChildStats(childId));
                    }
                })
                .addOnFailureListener(e -> updateStats(new ChildStats(childId)));

        // Load settings → kiểm tra AI
        childProfileRepository.getChildSettings(childId)
                .addOnSuccessListener(doc -> {
                    ChildSettings settings = DocumentMapper.toChildSettings(doc);
                    if (settings != null) {
                        aiEnabled = settings.isAiEnabled();
                        updateAiStatus();
                    }
                })
                .addOnFailureListener(e -> { /* giữ mặc định aiEnabled=true */ });
    }

    private void updateGreeting(ChildProfile profile) {
        String displayName = profile.getDisplayName();
        tvGreeting.setText(getString(R.string.child_home_greeting, displayName));
    }

    private void updateStats(ChildStats stats) {
        tvTotalPoints.setText(String.valueOf(stats.getTotalPoints()));
        tvStreak.setText(String.valueOf(stats.getStreakDays()));
        tvCompleted.setText(String.valueOf(stats.getTotalCompleted()));
    }

    private void updateAiStatus() {
        if (!aiEnabled) {
            tvAiStatus.setText("Tính năng AI bị tắt bởi phụ huynh 🔒");
            cardAiAssistant.setAlpha(0.6f);
        } else {
            tvAiStatus.setText("Hỏi tôi bất cứ điều gì nhé! 💬");
            cardAiAssistant.setAlpha(1f);
        }
    }

    // ==================== NAVIGATION ====================

    /**
     * Mở module placeholder.
     * Truyền thông tin để placeholder hiển thị đúng icon + mô tả.
     */
    private void openModule(Class<?> moduleClass, String icon, String title, String step) {
        Intent intent = new Intent(this, moduleClass);
        intent.putExtra("module_icon", icon);
        intent.putExtra("module_title", title);
        intent.putExtra("module_step", step);
        intent.putExtra(AppConstants.KEY_CHILD_ID, childId);
        startActivity(intent);
    }

    private void openAiChat() {
        if (!aiEnabled) {
            showToast("Trợ lý AI đã bị tắt. Phụ huynh có thể bật lại trong Cài đặt 🔒");
            return;
        }
        Intent intent = new Intent(this, AiChatActivity.class);
        intent.putExtra(AppConstants.KEY_CHILD_ID, childId);
        startActivity(intent);
    }

    private void openParentSettings() {
        // Settings cho bé này — mở ChildSettingsActivity của module parent
        Intent intent = new Intent(this, com.example.kid_app.parent.ChildSettingsActivity.class);
        intent.putExtra(AppConstants.KEY_CHILD_ID, childId);
        startActivity(intent);
    }

    // ==================== HELPERS ====================

    private void setTodayDate() {
        String[] days = {"Chủ nhật", "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm", "Thứ sáu", "Thứ bảy"};
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0=CN, 1=T2...
        String dayName = days[dayOfWeek];
        String dateStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.getTime());
        tvDate.setText(dayName + ", " + dateStr);
    }
}

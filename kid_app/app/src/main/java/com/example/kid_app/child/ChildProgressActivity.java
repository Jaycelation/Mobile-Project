package com.example.kid_app.child;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.model.ChildStats;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class ChildProgressActivity extends BaseActivity {

    private TextView tvPoints, tvBadges, tvStreak, tvLevelName, tvLevelTitle, tvMascot;
    private ImageView ivBadgeStar, ivBadgePainter;
    private String selectedChildId;
    private String childClassId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_progress);

        initViews();
        bindBottomNavigation();
        
        SharedPreferences prefs = getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        selectedChildId = prefs.getString(AppConstants.PREF_SELECTED_CHILD_ID, null);

        if (selectedChildId != null) {
            loadChildInfo();
            loadStats();
        } else {
            Toast.makeText(this, "Không tìm thấy hồ sơ bé", Toast.LENGTH_SHORT).show();
            finish();
        }

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        View viewAllBadges = findViewById(R.id.tv_view_all_badges);
        if (viewAllBadges != null) {
            viewAllBadges.setOnClickListener(v -> {
                startActivity(new Intent(this, BadgeCollectionActivity.class));
            });
        }
    }

    private void initViews() {
        tvPoints = findViewById(R.id.tv_points_earned);
        tvBadges = findViewById(R.id.tv_badges_earned);
        tvStreak = findViewById(R.id.tv_streak_count);
        tvLevelName = findViewById(R.id.tv_level_name);
        tvLevelTitle = findViewById(R.id.tv_level_title);
        tvMascot = findViewById(R.id.tv_mascot_quote);

        ivBadgeStar = findViewById(R.id.iv_badge_star);
        ivBadgePainter = findViewById(R.id.iv_badge_painter);
    }

    private void loadChildInfo() {
        // Chuc nang: goi Firestore de doc hoac ghi du lieu cho chuc nang hien tai.
        FirebaseFirestore.getInstance().collection(AppConstants.COL_CHILD_PROFILES)
                .document(selectedChildId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        childClassId = doc.getString("currentClassId");
                        if (childClassId == null || childClassId.isEmpty()) {
                            childClassId = doc.getString("classId");
                        }
                    }
                });
    }

    private void loadStats() {
        FirebaseFirestore.getInstance()
                .collection(AppConstants.COL_CHILD_STATS)
                .document(selectedChildId)
                .addSnapshotListener((doc, e) -> {
                    if (doc != null && doc.exists()) {
                        ChildStats stats = doc.toObject(ChildStats.class);
                        if (stats != null) {
                            int totalPoints = stats.getTotalPoints();
                            tvPoints.setText(String.format(Locale.getDefault(), "%,d", totalPoints));
                            tvStreak.setText(String.format(Locale.getDefault(), "🔥 %d Ngày", stats.getStreakDays()));
                            
                            int level = (totalPoints / 200) + 1;
                            int nextLevelPoints = level * 200;
                            int pointsNeeded = nextLevelPoints - totalPoints;
                            
                            tvLevelName.setText("Cấp độ " + level);
                            tvLevelTitle.setText(getLevelTitle(level));
                            tvMascot.setText("Bé ơi, chỉ cần thêm " + pointsNeeded + " điểm nữa là mình lên " + getLevelTitle(level + 1) + " rồi đó! ✨");
                            
                            // CẬP NHẬT: Đếm số lượng huy hiệu dựa trên điểm thực tế
                            updateBadgesAndCount(totalPoints);
                        }
                    }
                });
    }

    private void updateBadgesAndCount(int totalPoints) {
        int count = 0;
        
        // Kiểm tra mốc 20 điểm (Huy hiệu Chăm ngoan)
        boolean hasStar = totalPoints >= 20;
        updateBadgeUI(ivBadgeStar, hasStar);
        if (hasStar) count++;

        // Kiểm tra mốc 500 điểm (Huy hiệu Họa sĩ nhí)
        boolean hasPainter = totalPoints >= 500;
        updateBadgeUI(ivBadgePainter, hasPainter);
        if (hasPainter) count++;

        // Cập nhật con số hiển thị trên ô xanh
        tvBadges.setText(String.valueOf(count));
    }

    private void updateBadgeUI(ImageView iv, boolean isAchieved) {
        if (iv == null) return;
        if (isAchieved) {
            iv.clearColorFilter();
            iv.setAlpha(1.0f);
        } else {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0); 
            iv.setColorFilter(new ColorMatrixColorFilter(matrix));
            iv.setAlpha(0.4f);
        }
    }

    private String getLevelTitle(int level) {
        if (level < 3) return "Tập Sự Nhí";
        if (level < 6) return "Thám Hiểm Nhí";
        if (level < 10) return "Chiến Binh Sáng Tạo";
        return "Bậc Thầy Trí Tuệ";
    }

    private void bindBottomNavigation() {
        findViewById(R.id.nav_home).setOnClickListener(v -> finish());
        
        // ĐÃ SỬA: Chuyển hướng tới LeaderboardActivity (Bảng xếp hạng)
        findViewById(R.id.nav_community).setOnClickListener(v -> {
            if (isJoinedClass()) {
                startActivity(new Intent(this, LeaderboardActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Tính năng này yêu cầu bé tham gia lớp học trước nhé!", Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            startActivity(new Intent(this, ChildProfileActivity.class));
            finish();
        });
    }

    private boolean isJoinedClass() {
        return childClassId != null && !childClassId.isEmpty();
    }
}

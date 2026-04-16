package com.example.kid_app.child;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class BadgeCollectionActivity extends BaseActivity {

    private TextView tvBadgeSummary, tvPointsNeeded;
    private FirebaseFirestore db;
    private String selectedChildId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badge_collection);

        db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        selectedChildId = prefs.getString(AppConstants.PREF_SELECTED_CHILD_ID, null);

        initViews();
        if (selectedChildId != null) {
            loadStatsAndBadges();
        }
    }

    private void initViews() {
        tvBadgeSummary = findViewById(R.id.tv_badge_summary);
        tvPointsNeeded = findViewById(R.id.tv_points_needed);

        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void loadStatsAndBadges() {
        db.collection(AppConstants.COL_CHILD_STATS)
                .document(selectedChildId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        long points = 0;
                        if (doc.contains("totalPoints")) {
                            points = doc.getLong("totalPoints");
                        }
                        updateUI(points);
                    } else {
                        updateUI(0);
                    }
                });
    }

    private void updateUI(long points) {
        int unlockedCount = 0;
        String currentBadge = "Bé mới bắt đầu 🌱";
        long nextMilestone = 20;

        // Mốc 20
        if (points >= 20) {
            unlockBadge(R.id.layout_badge_20, R.id.iv_badge_20, R.id.tv_badge_20_name);
            unlockedCount++;
            currentBadge = "Chăm ngoan ⭐";
            nextMilestone = 500;
        }

        // Mốc 500
        if (points >= 500) {
            unlockBadge(R.id.layout_badge_500, R.id.iv_badge_500, R.id.tv_badge_500_name);
            unlockedCount++;
            currentBadge = "Họa sĩ nhí 🎨";
            nextMilestone = 1000;
        }

        // Mốc 1000
        if (points >= 1000) {
            unlockBadge(R.id.layout_badge_1000, R.id.iv_badge_1000, R.id.tv_badge_1000_name);
            unlockedCount++;
            currentBadge = "Học giả nhí 📚";
            nextMilestone = 2000;
        }

        // Mốc 2000
        if (points >= 2000) {
            unlockBadge(R.id.layout_badge_2000, R.id.iv_badge_2000, R.id.tv_badge_2000_name);
            unlockedCount++;
            currentBadge = "Bậc thầy kiến thức 🎓";
            nextMilestone = 5000;
        }

        // Mốc 5000
        if (points >= 5000) {
            unlockBadge(R.id.layout_badge_5000, R.id.iv_badge_5000, R.id.tv_badge_5000_name);
            unlockedCount++;
            currentBadge = "Siêu nhân trí tuệ 🚀";
            nextMilestone = 10000;
        }

        // Mốc 10000
        if (points >= 10000) {
            unlockBadge(R.id.layout_badge_10000, R.id.iv_badge_10000, R.id.tv_badge_10000_name);
            unlockedCount++;
            currentBadge = "Huyền thoại KidLearn 👑";
            nextMilestone = -1;
        }

        // Cập nhật thẻ tổng hợp
        tvBadgeSummary.setText(String.format(Locale.getDefault(), "Danh hiệu: %s", currentBadge));
        
        if (nextMilestone != -1) {
            long diff = nextMilestone - points;
            tvPointsNeeded.setText(String.format(Locale.getDefault(), "Cần thêm %d điểm để lên danh hiệu mới! 🔥", diff > 0 ? diff : 0));
        } else {
            tvPointsNeeded.setText("Bé đã đạt danh hiệu cao nhất! Chúc mừng bé! 🎉");
        }
    }

    private void unlockBadge(int layoutId, int ivId, int tvId) {
        LinearLayout layout = findViewById(layoutId);
        ImageView iv = findViewById(ivId);
        TextView tv = findViewById(tvId);

        if (layout != null) layout.setAlpha(1.0f);
        if (tv != null) tv.setTextColor(getResources().getColor(R.color.primary_green_dark));
        
        if (iv != null) {
            iv.setColorFilter(null); 
            if (ivId == R.id.iv_badge_20) iv.setColorFilter(getResources().getColor(R.color.secondary_orange), android.graphics.PorterDuff.Mode.SRC_IN);
            if (ivId == R.id.iv_badge_500) iv.setColorFilter(getResources().getColor(R.color.accent_blue), android.graphics.PorterDuff.Mode.SRC_IN);
            if (ivId == R.id.iv_badge_1000) iv.setColorFilter(getResources().getColor(R.color.primary_green), android.graphics.PorterDuff.Mode.SRC_IN);
            if (ivId == R.id.iv_badge_2000) iv.setColorFilter(Color.parseColor("#8E24AA"), android.graphics.PorterDuff.Mode.SRC_IN);
            if (ivId == R.id.iv_badge_5000) iv.setColorFilter(Color.parseColor("#00897B"), android.graphics.PorterDuff.Mode.SRC_IN);
            if (ivId == R.id.iv_badge_10000) iv.setColorFilter(Color.parseColor("#FBC02D"), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }
}

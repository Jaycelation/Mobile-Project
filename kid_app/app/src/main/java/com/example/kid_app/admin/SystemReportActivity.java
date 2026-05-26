package com.example.kid_app.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SystemReportActivity extends BaseActivity {

    private TextView tvTotalUsers, tvParents, tvTeachers;
    private TextView tvTotalContent, tvTotalAttempts;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_report);

        // Chuc nang: khoi tao Firestore de doc ghi du lieu cloud cho man hinh.
        db = FirebaseFirestore.getInstance();
        initViews();
        loadReports();
    }

    private void initViews() {
        tvTotalUsers = findViewById(R.id.tv_report_total_users);
        tvParents = findViewById(R.id.tv_report_parents);
        tvTeachers = findViewById(R.id.tv_report_teachers);
        tvTotalContent = findViewById(R.id.tv_report_total_content);
        tvTotalAttempts = findViewById(R.id.tv_report_total_attempts);
        progressBar = findViewById(R.id.progress_bar);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void loadReports() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // 1. Thống kê người dùng (Lấy thực tế từ Firebase)
        // Chuc nang: goi Firestore de doc hoac ghi du lieu cho chuc nang hien tai.
        db.collection(AppConstants.COL_ACCOUNTS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int total = querySnapshot.size();
                    int parents = 0;
                    int teachers = 0;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String role = doc.getString("role");
                        if (AppConstants.ROLE_PARENT.equals(role)) parents++;
                        else if (AppConstants.ROLE_TEACHER.equals(role)) teachers++;
                    }
                    tvTotalUsers.setText(String.valueOf(total));
                    tvParents.setText(String.valueOf(parents));
                    tvTeachers.setText(String.valueOf(teachers));
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                });

        // 2. Cập nhật con số theo yêu cầu của em
        // Tổng số trò học tập: 6
        tvTotalContent.setText("6");

        // Tổng số trò thực hành: 10
        tvTotalAttempts.setText("10");
    }
}

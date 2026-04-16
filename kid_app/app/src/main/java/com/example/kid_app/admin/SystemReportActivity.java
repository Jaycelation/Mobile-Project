package com.example.kid_app.admin;

import android.os.Bundle;
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
        progressBar.setVisibility(View.VISIBLE);

        // 1. Thống kê người dùng
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
                });

        // 2. Thống kê nội dung
        db.collection(AppConstants.COL_CONTENT_CATALOG)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    tvTotalContent.setText(String.valueOf(querySnapshot.size()));
                });

        // 3. Thống kê lượt học (Sử dụng collectionGroup để quét tất cả subcollections)
        db.collectionGroup(AppConstants.SUBCOL_ACTIVITY_ATTEMPTS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    tvTotalAttempts.setText(String.valueOf(querySnapshot.size()));
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Lỗi tải báo cáo lượt học");
                });
    }
}

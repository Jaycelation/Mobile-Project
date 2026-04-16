package com.example.kid_app.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kid_app.R;
import com.example.kid_app.auth.AuthService;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.mapper.DocumentMapper;
import com.example.kid_app.data.model.Account;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TeacherProfileActivity extends BaseActivity {

    private static final String TAG = "TeacherProfileActivity";
    private TextView tvName, tvStatClasses, tvStatStudents, tvStatAssignments;
    private AuthService authService;
    private FirebaseFirestore db;
    private ListenerRegistration classesListener, assignmentsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_profile);

        authService = new AuthService();
        db = FirebaseFirestore.getInstance();

        bindViews();
        setupMenu();
        loadTeacherInfo();
        loadStatsRealtime(); 
        bindBottomNavigation();
    }

    private void bindViews() {
        tvName = findViewById(R.id.tv_teacher_name);
        tvStatClasses = findViewById(R.id.tv_stat_classes);
        tvStatStudents = findViewById(R.id.tv_stat_students);
        tvStatAssignments = findViewById(R.id.tv_stat_assignments);

        tvStatClasses.setText("--");
        tvStatStudents.setText("--");
        tvStatAssignments.setText("--");

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            authService.signOut();
            navigateToClearStack(com.example.kid_app.WelcomeActivity.class);
        });
    }

    private void setupMenu() {
        setupMenuItem(R.id.menu_edit_info, "Chỉnh sửa thông tin")
            .setOnClickListener(v -> startActivity(new Intent(this, EditTeacherProfileActivity.class)));

        setupMenuItem(R.id.menu_change_password, "Đổi mật khẩu")
            .setOnClickListener(v -> {
                String email = authService.getCurrentUser() != null ? authService.getCurrentUser().getEmail() : null;
                if (email != null) {
                    authService.sendPasswordResetEmail(email)
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã gửi email đổi mật khẩu!", Toast.LENGTH_LONG).show());
                }
            });

        setupMenuItem(R.id.menu_settings, "Cài đặt thông báo");
        setupMenuItem(R.id.menu_help, "Trợ giúp & Hỗ trợ");
    }

    private View setupMenuItem(int id, String title) {
        View view = findViewById(id);
        TextView tvTitle = view.findViewById(R.id.tv_menu_title);
        if (tvTitle != null) tvTitle.setText(title);
        return view;
    }

    private void loadTeacherInfo() {
        authService.getCurrentUserAccount()
                .addOnSuccessListener(doc -> {
                    Account account = DocumentMapper.toAccount(doc);
                    if (account != null) tvName.setText(account.getFullName());
                });
    }

    private void loadStatsRealtime() {
        String teacherId = authService.getCurrentUser() != null ? authService.getCurrentUser().getUid() : "";
        if (teacherId.isEmpty()) return;

        classesListener = db.collection("classes")
                .whereEqualTo("teacherId", teacherId)
                .addSnapshotListener((snap, e) -> {
                    if (snap != null) {
                        tvStatClasses.setText(String.format(Locale.getDefault(), "%02d", snap.size()));
                        
                        if (snap.isEmpty()) {
                            tvStatStudents.setText("0");
                            return;
                        }

                        List<String> classIds = new ArrayList<>();
                        for (DocumentSnapshot doc : snap.getDocuments()) {
                            classIds.add(doc.getId());
                        }

                        db.collection("class_members")
                                .whereIn("classId", classIds)
                                .get()
                                .addOnSuccessListener(memberSnap -> {
                                    Set<String> uniqueChildIds = new HashSet<>();
                                    for (DocumentSnapshot doc : memberSnap.getDocuments()) {
                                        String cid = doc.getString("childId");
                                        if (cid != null) uniqueChildIds.add(cid);
                                    }
                                    tvStatStudents.setText(String.valueOf(uniqueChildIds.size()));
                                })
                                .addOnFailureListener(err -> Log.e(TAG, "Error counting students", err));
                    }
                });

        assignmentsListener = db.collection("assignments")
                .whereEqualTo("teacherId", teacherId)
                .addSnapshotListener((snap, e) -> {
                    if (snap != null) {
                        tvStatAssignments.setText(String.valueOf(snap.size()));
                    }
                });
    }

    private void bindBottomNavigation() {
        findViewById(R.id.nav_home).setOnClickListener(v -> finish());
        findViewById(R.id.nav_classes).setOnClickListener(v -> {
            startActivity(new Intent(this, ClassManagementActivity.class));
            finish();
        });
        findViewById(R.id.nav_assignments).setOnClickListener(v -> {
            startActivity(new Intent(this, AssignmentManagementActivity.class));
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        if (classesListener != null) classesListener.remove();
        if (assignmentsListener != null) assignmentsListener.remove();
        super.onDestroy();
    }
}

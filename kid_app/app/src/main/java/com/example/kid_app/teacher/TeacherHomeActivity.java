package com.example.kid_app.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kid_app.R;
import com.example.kid_app.auth.AuthService;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.mapper.DocumentMapper;
import com.example.kid_app.data.model.Account;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TeacherHomeActivity extends BaseActivity {

    private static final String TAG = "TeacherHomeActivity";
    private AuthService authService;
    private TextView tvTeacherName, tvClassCount, tvStudentCount, tvEmptySubmissions;
    private RecyclerView rvLatestSubmissions;
    private SubmissionAdapter submissionAdapter;
    private List<Map<String, Object>> latestSubmissions = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_home);

        authService = new AuthService();
        db = FirebaseFirestore.getInstance();

        bindViews();
        loadUserInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUIWithRealData();
        loadLatestSubmissions();
    }

    private void bindViews() {
        tvTeacherName = findViewById(R.id.tv_teacher_name);
        tvClassCount = findViewById(R.id.tv_class_count);
        tvStudentCount = findViewById(R.id.tv_student_count);
        tvEmptySubmissions = findViewById(R.id.tv_empty_submissions);
        rvLatestSubmissions = findViewById(R.id.rv_latest_submissions);

        rvLatestSubmissions.setLayoutManager(new LinearLayoutManager(this));
        submissionAdapter = new SubmissionAdapter(latestSubmissions);
        rvLatestSubmissions.setAdapter(submissionAdapter);
        
        findViewById(R.id.btn_quick_create_class).setOnClickListener(v -> startActivity(new Intent(this, CreateClassActivity.class)));
        findViewById(R.id.btn_quick_assignment).setOnClickListener(v -> startActivity(new Intent(this, CreateAssignmentActivity.class)));
        findViewById(R.id.btn_quick_feedback).setOnClickListener(v -> startActivity(new Intent(this, FeedbackListActivity.class)));

        findViewById(R.id.nav_classes).setOnClickListener(v -> startActivity(new Intent(this, ClassManagementActivity.class)));
        findViewById(R.id.nav_assignments).setOnClickListener(v -> startActivity(new Intent(this, AssignmentManagementActivity.class)));
        findViewById(R.id.nav_profile).setOnClickListener(v -> startActivity(new Intent(this, TeacherProfileActivity.class)));
    }

    private void loadUserInfo() {
        authService.getCurrentUserAccount()
                .addOnSuccessListener(doc -> {
                    Account account = DocumentMapper.toAccount(doc);
                    if (account != null && account.getFullName() != null) {
                        tvTeacherName.setText(account.getFullName());
                    }
                });
    }

    private void updateUIWithRealData() {
        String teacherId = authService.getCurrentUser() != null ? authService.getCurrentUser().getUid() : "";
        if (teacherId.isEmpty()) return;

        db.collection("classes")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(classSnap -> {
                    int classCount = classSnap.size();
                    if (tvClassCount != null) {
                        tvClassCount.setText(String.format(Locale.getDefault(), "%d", classCount));
                    }

                    if (classCount == 0) {
                        if (tvStudentCount != null) tvStudentCount.setText("0");
                        return;
                    }

                    List<String> classIds = new ArrayList<>();
                    for (DocumentSnapshot doc : classSnap.getDocuments()) {
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
                                if (tvStudentCount != null) {
                                    tvStudentCount.setText(String.valueOf(uniqueChildIds.size()));
                                }
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Error counting students", e));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error counting classes", e));
    }

    private void loadLatestSubmissions() {
        String teacherId = authService.getCurrentUser() != null ? authService.getCurrentUser().getUid() : "";
        if (teacherId.isEmpty()) return;

        // B1: Lấy danh sách ID các bài tập thầy đã giao
        db.collection("assignments")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(assignmentSnap -> {
                    if (assignmentSnap.isEmpty()) {
                        tvEmptySubmissions.setVisibility(View.VISIBLE);
                        return;
                    }

                    List<String> assignmentIds = new ArrayList<>();
                    for (DocumentSnapshot doc : assignmentSnap) {
                        assignmentIds.add(doc.getId());
                    }

                    // B2: Lấy 5 bài nộp mới nhất của các bài tập này
                    db.collection("assignment_submissions")
                            .whereIn("assignmentId", assignmentIds)
                            .orderBy("completedAt", Query.Direction.DESCENDING)
                            .limit(5)
                            .get()
                            .addOnSuccessListener(subSnap -> {
                                latestSubmissions.clear();
                                if (subSnap.isEmpty()) {
                                    tvEmptySubmissions.setVisibility(View.VISIBLE);
                                } else {
                                    tvEmptySubmissions.setVisibility(View.GONE);
                                    for (DocumentSnapshot doc : subSnap) {
                                        Map<String, Object> data = doc.getData();
                                        latestSubmissions.add(data);
                                    }
                                }
                                submissionAdapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                tvEmptySubmissions.setVisibility(View.VISIBLE);
                                Log.e(TAG, "Error loading submissions", e);
                            });
                });
    }

    private class SubmissionAdapter extends RecyclerView.Adapter<SubmissionAdapter.ViewHolder> {
        private List<Map<String, Object>> list;
        public SubmissionAdapter(List<Map<String, Object>> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_latest_submission, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> item = list.get(position);
            String childId = String.valueOf(item.get("childId"));
            String assignmentId = String.valueOf(item.get("assignmentId"));
            int score = item.get("score") != null ? ((Long)item.get("score")).intValue() : 0;

            holder.tvScore.setText(score + "đ");
            
            // Fetch Child Name
            db.collection("child_profiles").document(childId).get().addOnSuccessListener(d -> {
                if (d.exists()) holder.tvName.setText(d.getString("fullName"));
            });

            // Fetch Assignment Title
            db.collection("assignments").document(assignmentId).get().addOnSuccessListener(d -> {
                if (d.exists()) holder.tvInfo.setText("Vừa hoàn thành: " + d.getString("title"));
            });
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvInfo, tvScore;
            public ViewHolder(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_child_name);
                tvInfo = v.findViewById(R.id.tv_submission_info);
                tvScore = v.findViewById(R.id.tv_score);
            }
        }
    }
}

package com.example.kid_app.teacher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.mapper.DocumentMapper;
import com.example.kid_app.data.model.ChildProfile;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AssignmentDetailActivity extends BaseActivity {

    private TextView tvTitleHeader, tvDueDate, tvSubmittedSummary, tvAverageScore, tvStatusTitle;
    private TextView btnFilterDone, btnFilterPending;
    private ProgressBar pbCompletion;
    private RecyclerView rvSubmissions;
    private SubmissionAdapter adapter;
    
    private List<ChildProfile> allStudents = new ArrayList<>();
    private List<ChildProfile> displayedStudents = new ArrayList<>();
    private Map<String, Integer> studentScores = new HashMap<>();
    
    private FirebaseFirestore db;
    private String assignmentId, classId;
    private boolean showingDone = true;
    private ListenerRegistration submissionsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_detail);

        db = FirebaseFirestore.getInstance();
        assignmentId = getIntent().getStringExtra("assignment_id");

        bindViews();
        loadAssignmentData();
    }

    private void bindViews() {
        tvTitleHeader = findViewById(R.id.tv_assignment_title_header);
        tvDueDate = findViewById(R.id.tv_due_date_info);
        tvSubmittedSummary = findViewById(R.id.tv_submitted_summary);
        tvAverageScore = findViewById(R.id.tv_average_score);
        tvStatusTitle = findViewById(R.id.tv_status_title);
        pbCompletion = findViewById(R.id.pb_completion_percent);
        
        btnFilterDone = findViewById(R.id.btn_filter_done);
        btnFilterPending = findViewById(R.id.btn_filter_pending);

        rvSubmissions = findViewById(R.id.rv_submissions);
        rvSubmissions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubmissionAdapter(displayedStudents);
        rvSubmissions.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnFilterDone.setOnClickListener(v -> {
            showingDone = true;
            updateTabUI();
            filterList();
        });

        btnFilterPending.setOnClickListener(v -> {
            showingDone = false;
            updateTabUI();
            filterList();
        });
    }

    private void loadAssignmentData() {
        if (assignmentId == null) return;

        db.collection("assignments").document(assignmentId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        if (tvTitleHeader != null) tvTitleHeader.setText(doc.getString("title"));
                        if (tvDueDate != null) tvDueDate.setText("📅 Hạn nộp: " + doc.getString("dueDate"));
                        classId = doc.getString("classId");
                        loadClassStudents();
                    }
                });
    }

    private void loadClassStudents() {
        if (classId == null) return;

        db.collection("class_members")
                .whereEqualTo("classId", classId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allStudents.clear();
                    final int total = queryDocumentSnapshots.size();
                    if (total == 0) {
                        updateStatsUI(0, 0);
                        return;
                    }

                    final int[] loadedCount = {0};
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String childId = doc.getString("childId");
                        db.collection("child_profiles").document(childId).get()
                                .addOnSuccessListener(childDoc -> {
                                    ChildProfile profile = DocumentMapper.toChildProfile(childDoc);
                                    if (profile != null) allStudents.add(profile);
                                    
                                    loadedCount[0]++;
                                    if (loadedCount[0] == total) {
                                        listenToSubmissions();
                                    }
                                });
                    }
                });
    }

    private void listenToSubmissions() {
        if (submissionsListener != null) submissionsListener.remove();

        submissionsListener = db.collection("assignment_submissions")
                .whereEqualTo("assignmentId", assignmentId)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        studentScores.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            String cId = doc.getString("childId");
                            Long score = doc.getLong("score");
                            if (cId != null) {
                                studentScores.put(cId, score != null ? score.intValue() : 0);
                            }
                        }
                        updateStatsUI(allStudents.size(), studentScores.size());
                        filterList();
                    }
                });
    }

    private void updateStatsUI(int total, int submitted) {
        if (tvSubmittedSummary != null) tvSubmittedSummary.setText("Đã làm bài: " + submitted + "/" + total + " học sinh");
        
        // THAY ĐỔI: Thanh tiến độ hiển thị tỉ lệ hoàn thành của cả lớp
        if (pbCompletion != null) {
            pbCompletion.setMax(total > 0 ? total : 100);
            pbCompletion.setProgress(submitted);
        }
        
        if (btnFilterDone != null) btnFilterDone.setText("Đã làm (" + submitted + ")");
        if (btnFilterPending != null) btnFilterPending.setText("Chưa làm (" + (total - submitted) + ")");
        
        // THAY ĐỔI: Điểm trung bình chỉ tính trên những bé đã nộp bài
        double avg = 0;
        if (!studentScores.isEmpty()) {
            int sum = 0;
            for (int s : studentScores.values()) sum += s;
            avg = (double) sum / studentScores.size();
        }
        if (tvAverageScore != null) tvAverageScore.setText(String.format(Locale.getDefault(), "⭐ Điểm trung bình: %.1f", avg));
    }

    private void filterList() {
        displayedStudents.clear();
        for (ChildProfile student : allStudents) {
            boolean isSubmitted = studentScores.containsKey(student.getChildId());
            if (showingDone && isSubmitted) {
                displayedStudents.add(student);
            } else if (!showingDone && !isSubmitted) {
                displayedStudents.add(student);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateTabUI() {
        btnFilterDone.setAlpha(showingDone ? 1.0f : 0.6f);
        btnFilterPending.setAlpha(showingDone ? 0.6f : 1.0f);
        if (tvStatusTitle != null) tvStatusTitle.setText(showingDone ? "Danh sách đã hoàn thành" : "Danh sách chưa hoàn thành");
    }

    @Override
    protected void onDestroy() {
        if (submissionsListener != null) submissionsListener.remove();
        super.onDestroy();
    }

    private class SubmissionAdapter extends RecyclerView.Adapter<SubmissionAdapter.ViewHolder> {
        private List<ChildProfile> list;
        public SubmissionAdapter(List<ChildProfile> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_in_class, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChildProfile item = list.get(position);
            holder.tvName.setText(item.getFullName());
            holder.tvId.setText("Mã bé: " + (item.getChildId() != null ? item.getChildId().substring(0,6).toUpperCase() : "N/A"));
            
            if (studentScores.containsKey(item.getChildId())) {
                int score = studentScores.get(item.getChildId());
                holder.tvStatus.setVisibility(View.VISIBLE);
                holder.tvStatus.setText("✅ Điểm đạt được: " + score + "/10");
                holder.tvStatus.setTextColor(getResources().getColor(R.color.primary_green_dark));
            } else {
                holder.tvStatus.setVisibility(View.VISIBLE);
                holder.tvStatus.setText("⚠️ Chưa nộp bài");
                holder.tvStatus.setTextColor(getResources().getColor(R.color.secondary_orange));
            }
            if (holder.btnAction != null) holder.btnAction.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvId, tvStatus;
            View btnAction;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_student_name);
                tvId = itemView.findViewById(R.id.tv_parent_info);
                tvStatus = itemView.findViewById(R.id.tv_status_text);
                btnAction = itemView.findViewById(R.id.btn_student_action);
            }
        }
    }
}

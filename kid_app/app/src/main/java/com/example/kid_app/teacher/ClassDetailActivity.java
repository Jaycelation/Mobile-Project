package com.example.kid_app.teacher;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kid_app.R;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.mapper.DocumentMapper;
import com.example.kid_app.data.model.ChildProfile;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ClassDetailActivity extends BaseActivity {

    private TextView tvClassName, tvTeacherClassInfo;
    private TextView tvOpenAssignmentsCount, tvCompletedCount, tvPendingCount;
    private RecyclerView rvStudents;
    private StudentAdapter adapter;
    private List<ChildProfile> studentList = new ArrayList<>();
    private FirebaseFirestore db;
    private String classId, className, joinCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail);

        db = FirebaseFirestore.getInstance();
        classId = getIntent().getStringExtra("class_id");
        className = getIntent().getStringExtra("class_name");
        joinCode = getIntent().getStringExtra("join_code"); // Nhận joinCode từ danh sách lớp

        bindViews();
        setupInitialData();
        loadRealData();
    }

    private void bindViews() {
        tvClassName = findViewById(R.id.tv_class_name);
        tvTeacherClassInfo = findViewById(R.id.tv_teacher_class_info);
        
        tvOpenAssignmentsCount = findViewById(R.id.tv_open_assignments_count);
        tvCompletedCount = findViewById(R.id.tv_completed_count);
        tvPendingCount = findViewById(R.id.tv_pending_count);

        rvStudents = findViewById(R.id.rv_students);
        if (rvStudents != null) {
            rvStudents.setLayoutManager(new LinearLayoutManager(this));
            adapter = new StudentAdapter(studentList);
            rvStudents.setAdapter(adapter);
        }

        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        
        View btnAdd = findViewById(R.id.btn_add_student);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddStudentActivity.class);
                intent.putExtra("class_id", classId);
                intent.putExtra("join_code", joinCode); // TRUYỀN MÃ LỚP SANG ĐỂ HIỆN QR VÀ COPY
                startActivity(intent);
            });
        }
    }

    private void setupInitialData() {
        if (className != null && tvClassName != null) tvClassName.setText(className);
        
        // Nếu joinCode chưa có (do mở từ một luồng khác), tải nó từ DB
        if (joinCode == null && classId != null) {
            db.collection("classes").document(classId).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    joinCode = doc.getString("joinCode");
                }
            });
        }
    }

    private void loadRealData() {
        if (classId == null) return;

        db.collection("class_members")
                .whereEqualTo("classId", classId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    studentList.clear();
                    final int totalStudents = queryDocumentSnapshots.size();
                    updateHeaderInfo(totalStudents);

                    if (totalStudents == 0) {
                        if (adapter != null) adapter.notifyDataSetChanged();
                        updateStats(0, 0, 0);
                        return;
                    }

                    final int[] count = {0};
                    for (DocumentSnapshot memberDoc : queryDocumentSnapshots) {
                        String childId = memberDoc.getString("childId");
                        if (childId != null) {
                            db.collection("child_profiles").document(childId).get()
                                    .addOnSuccessListener(childDoc -> {
                                        ChildProfile profile = DocumentMapper.toChildProfile(childDoc);
                                        if (profile != null) studentList.add(profile);
                                        count[0]++;
                                        if (count[0] == totalStudents) {
                                            if (adapter != null) adapter.notifyDataSetChanged();
                                            loadAssignmentStats(totalStudents);
                                        }
                                    });
                        } else {
                            count[0]++;
                        }
                    }
                });
    }

    private void loadAssignmentStats(int totalStudents) {
        db.collection("assignments")
                .whereEqualTo("classId", classId)
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(snap -> {
                    int openAssignments = snap.size();
                    if (openAssignments == 0) {
                        updateStats(0, 0, totalStudents);
                        if (adapter != null) adapter.updateProgress(0, new HashMap<>());
                        return;
                    }

                    List<String> assignmentIds = new ArrayList<>();
                    for (DocumentSnapshot doc : snap) assignmentIds.add(doc.getId());

                    Map<String, Integer> studentProgressMap = new HashMap<>();
                    final int[] processedCount = {0};

                    for (String aId : assignmentIds) {
                        db.collection("assignment_submissions")
                                .whereEqualTo("assignmentId", aId)
                                .whereEqualTo("status", "submitted")
                                .get()
                                .addOnSuccessListener(subSnap -> {
                                    for (DocumentSnapshot subDoc : subSnap) {
                                        String cId = subDoc.getString("childId");
                                        if (cId != null) {
                                            studentProgressMap.put(cId, studentProgressMap.getOrDefault(cId, 0) + 1);
                                        }
                                    }
                                    processedCount[0]++;
                                    if (processedCount[0] == openAssignments) {
                                        int fullyCompletedCount = 0;
                                        for (ChildProfile student : studentList) {
                                            Integer done = studentProgressMap.get(student.getChildId());
                                            if (done != null && done >= openAssignments) {
                                                fullyCompletedCount++;
                                            }
                                        }
                                        updateStats(openAssignments, fullyCompletedCount, totalStudents);
                                        if (adapter != null) {
                                            adapter.updateProgress(openAssignments, studentProgressMap);
                                        }
                                    }
                                });
                    }
                });
    }

    private void updateStats(int openAssignments, int completed, int total) {
        if (tvOpenAssignmentsCount != null) tvOpenAssignmentsCount.setText(String.valueOf(openAssignments));
        if (tvCompletedCount != null) tvCompletedCount.setText(completed + "/" + total);
        if (tvPendingCount != null) tvPendingCount.setText(String.valueOf(total - completed));
    }

    private void updateHeaderInfo(int count) {
        if (tvTeacherClassInfo != null) {
            tvTeacherClassInfo.setText("Quản lý lớp học • " + count + " học sinh");
        }
    }

    private void removeStudentFromClass(String childId, String childName) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa học sinh")
                .setMessage("Bạn có chắc chắn muốn mời bé " + childName + " ra khỏi lớp không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("class_members")
                            .whereEqualTo("classId", classId)
                            .whereEqualTo("childId", childId)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                    doc.getReference().delete();
                                }
                                Toast.makeText(this, "Đã mời bé " + childName + " ra khỏi lớp", Toast.LENGTH_SHORT).show();
                                loadRealData();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {
        private List<ChildProfile> list;
        private Map<String, Integer> progressMap = new HashMap<>();
        private int totalAssignments = 0;

        public StudentAdapter(List<ChildProfile> list) { this.list = list; }

        public void updateProgress(int total, Map<String, Integer> progress) {
            this.totalAssignments = total;
            this.progressMap = progress;
            notifyDataSetChanged();
        }

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
            holder.tvIdInfo.setText(String.format(Locale.getDefault(), "Mã bé: %s", 
                item.getChildId() != null && item.getChildId().length() > 6 ? 
                item.getChildId().substring(0, 6).toUpperCase() : "STUDENT"));
            
            holder.tvStatus.setVisibility(View.VISIBLE);
            
            int done = progressMap.getOrDefault(item.getChildId(), 0);
            if (totalAssignments > 0) {
                if (done >= totalAssignments) {
                    holder.tvStatus.setText("✅ Đã hoàn thành tất cả");
                    holder.tvStatus.setTextColor(getResources().getColor(R.color.primary_green));
                } else {
                    holder.tvStatus.setText("⚠️ Đã làm " + done + "/" + totalAssignments + " bài tập");
                    holder.tvStatus.setTextColor(getResources().getColor(R.color.secondary_orange));
                }
            } else {
                holder.tvStatus.setText("✨ Không có bài tập");
                holder.tvStatus.setTextColor(getResources().getColor(R.color.text_secondary));
            }
            
            if (holder.btnAction != null) {
                holder.btnAction.setImageResource(android.R.drawable.ic_menu_delete);
                holder.btnAction.setOnClickListener(v -> removeStudentFromClass(item.getChildId(), item.getFullName()));
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvIdInfo, tvStatus;
            ImageButton btnAction;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_student_name);
                tvIdInfo = itemView.findViewById(R.id.tv_parent_info);
                tvStatus = itemView.findViewById(R.id.tv_status_text);
                btnAction = itemView.findViewById(R.id.btn_student_action);
            }
        }
    }
}

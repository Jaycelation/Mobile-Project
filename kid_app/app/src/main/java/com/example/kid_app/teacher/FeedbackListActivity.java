package com.example.kid_app.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kid_app.R;
import com.example.kid_app.auth.AuthService;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.mapper.DocumentMapper;
import com.example.kid_app.data.model.ChildProfile;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FeedbackListActivity extends BaseActivity {

    private static final String TAG = "FeedbackListActivity";
    private RecyclerView rvFeedback;
    private StudentAdapter adapter;
    private List<ChildProfile> studentList = new ArrayList<>();
    private List<ChildProfile> filteredList = new ArrayList<>();
    private FirebaseFirestore db;
    private AuthService authService;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_list);

        db = FirebaseFirestore.getInstance();
        authService = new AuthService();

        bindViews();
        loadAllStudents();
    }

    private void bindViews() {
        rvFeedback = findViewById(R.id.rv_feedback);
        rvFeedback.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new StudentAdapter(filteredList);
        rvFeedback.setAdapter(adapter);

        etSearch = findViewById(R.id.et_search_input);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        // Đã xóa nút FAB và các Tab cũ trong layout XML theo yêu cầu mới
    }

    private void loadAllStudents() {
        String teacherId = authService.getCurrentUser() != null ? authService.getCurrentUser().getUid() : "";
        if (teacherId.isEmpty()) return;

        // B1: Lấy danh sách các lớp của giáo viên
        db.collection("classes")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(classSnap -> {
                    if (classSnap.isEmpty()) return;

                    List<String> classIds = new ArrayList<>();
                    for (DocumentSnapshot doc : classSnap) classIds.add(doc.getId());

                    // B2: Lấy tất cả thành viên trong các lớp này
                    db.collection("class_members")
                            .whereIn("classId", classIds)
                            .get()
                            .addOnSuccessListener(memberSnap -> {
                                Set<String> uniqueChildIds = new HashSet<>();
                                for (DocumentSnapshot doc : memberSnap) {
                                    String cid = doc.getString("childId");
                                    if (cid != null) uniqueChildIds.add(cid);
                                }

                                if (uniqueChildIds.isEmpty()) return;

                                // B3: Lấy hồ sơ của từng bé
                                studentList.clear();
                                final int total = uniqueChildIds.size();
                                final int[] count = {0};

                                for (String childId : uniqueChildIds) {
                                    db.collection("child_profiles").document(childId).get()
                                            .addOnSuccessListener(childDoc -> {
                                                ChildProfile profile = DocumentMapper.toChildProfile(childDoc);
                                                if (profile != null && !profile.isDeleted()) {
                                                    studentList.add(profile);
                                                }
                                                count[0]++;
                                                if (count[0] == total) {
                                                    filter(""); // Hiển thị tất cả ban đầu
                                                }
                                            });
                                }
                            });
                });
    }

    private void filter(String text) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(studentList);
        } else {
            String query = text.toLowerCase().trim();
            for (ChildProfile item : studentList) {
                if (item.getFullName().toLowerCase().contains(query)) {
                    filteredList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void openChat(ChildProfile child) {
        // Tìm hoặc tạo session chat cho bé này
        db.collection("feedback_notes")
                .whereEqualTo("childId", child.getChildId())
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    String feedbackId;
                    if (!snap.isEmpty()) {
                        feedbackId = snap.getDocuments().get(0).getId();
                        startChatActivity(feedbackId);
                    } else {
                        // Tạo session mới nếu chưa có
                        java.util.Map<String, Object> session = new java.util.HashMap<>();
                        session.put("childId", child.getChildId());
                        session.put("teacherId", authService.getCurrentUser().getUid());
                        session.put("noteText", "Thầy/cô vừa bắt đầu trao đổi");
                        session.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
                        
                        db.collection("feedback_notes").add(session).addOnSuccessListener(ref -> {
                            startChatActivity(ref.getId());
                        });
                    }
                });
    }

    private void startChatActivity(String fId) {
        Intent intent = new Intent(this, FeedbackChatActivity.class);
        intent.putExtra("feedback_id", fId);
        startActivity(intent);
    }

    private class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {
        private List<ChildProfile> list;
        public StudentAdapter(List<ChildProfile> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feedback, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChildProfile item = list.get(position);
            holder.tvName.setText(item.getFullName());
            holder.tvMessage.setText("Nhấn để trao đổi với phụ huynh");
            holder.tvTime.setText(item.getAgeGroup() != null ? "Tuổi: " + item.getAgeGroup() : "");
            holder.statusDot.setVisibility(View.GONE);

            holder.itemView.setOnClickListener(v -> openChat(item));
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvMessage, tvTime;
            View statusDot;
            public ViewHolder(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_sender_name);
                tvMessage = v.findViewById(R.id.tv_message);
                tvTime = v.findViewById(R.id.tv_time_status);
                statusDot = v.findViewById(R.id.view_status_dot);
            }
        }
    }
}

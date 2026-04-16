package com.example.kid_app.child;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChildAssignmentActivity extends BaseActivity {

    private RecyclerView rvAssignments;
    private AssignmentAdapter adapter;
    private List<Map<String, Object>> assignmentList = new ArrayList<>();
    private FirebaseFirestore db;
    private String classId;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_assignment);

        db = FirebaseFirestore.getInstance();
        classId = getIntent().getStringExtra("classId");

        rvAssignments = findViewById(R.id.rv_assignments);
        tvEmpty = findViewById(R.id.tv_empty);
        
        rvAssignments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AssignmentAdapter(assignmentList);
        rvAssignments.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadAssignments();
    }

    private void loadAssignments() {
        if (classId == null) {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        db.collection("assignments")
                .whereEqualTo("classId", classId)
                .whereEqualTo("status", "active")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        assignmentList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Map<String, Object> data = doc.getData();
                            data.put("id", doc.getId());
                            assignmentList.add(data);
                        }
                        adapter.notifyDataSetChanged();
                        tvEmpty.setVisibility(assignmentList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void startAssignmentGame(Map<String, Object> item) {
        String gameType = String.valueOf(item.get("gameType"));
        String assignmentId = String.valueOf(item.get("id"));
        
        Intent intent;
        switch (gameType) {
            case "Thử thách con số": intent = new Intent(this, NumberMatchGameActivity.class); break;
            case "Đếm số quả": intent = new Intent(this, CountingFruitGameActivity.class); break;
            case "Ghép hình bóng": intent = new Intent(this, ShadowMatchGameActivity.class); break;
            case "Đúng con vật": intent = new Intent(this, AnimalGameActivity.class); break;
            case "Đúng chữ cái": intent = new Intent(this, AlphabetMatchGameActivity.class); break;
            case "Đúng loại quả": intent = new Intent(this, FruitMatchGameActivity.class); break;
            case "Đúng màu sắc": intent = new Intent(this, ColorMatchGameActivity.class); break;
            case "Quy luật logic": intent = new Intent(this, PatternGameActivity.class); break;
            case "Nhanh tay tinh mắt": intent = new Intent(this, FastEyeGameActivity.class); break;
            case "Ghép tranh trí tuệ": intent = new Intent(this, PuzzleGameActivity.class); break;
            default:
                Toast.makeText(this, "Trò chơi này đang được cập nhật!", Toast.LENGTH_SHORT).show();
                return;
        }

        intent.putExtra(AppConstants.KEY_ASSIGNMENT_ID, assignmentId);
        intent.putExtra("isAssignmentMode", true); // Chế độ bài tập
        startActivity(intent);
    }

    private class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.ViewHolder> {
        private List<Map<String, Object>> list;
        public AssignmentAdapter(List<Map<String, Object>> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assignment_card, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> item = list.get(position);
            holder.tvTitle.setText(String.valueOf(item.getOrDefault("title", "Bài tập")));
            holder.tvClass.setText(String.valueOf(item.getOrDefault("gameType", "Thực hành")));
            holder.tvDueDate.setText("Hạn: " + item.getOrDefault("dueDate", "Không có"));
            
            holder.btnDetails.setOnClickListener(v -> startAssignmentGame(item));
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvClass, tvDueDate;
            View btnDetails;
            public ViewHolder(@NonNull View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_assignment_title);
                tvClass = v.findViewById(R.id.tv_class_tag);
                tvDueDate = v.findViewById(R.id.tv_due_date);
                btnDetails = v.findViewById(R.id.btn_details);
            }
        }
    }
}

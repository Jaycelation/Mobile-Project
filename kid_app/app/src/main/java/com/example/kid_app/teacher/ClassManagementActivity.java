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
import com.example.kid_app.auth.AuthService;
import com.example.kid_app.common.BaseActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClassManagementActivity extends BaseActivity {

    private RecyclerView rvClasses;
    private ClassAdapter adapter;
    private List<Map<String, Object>> classList = new ArrayList<>();
    private FirebaseFirestore db;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_management);

        db = FirebaseFirestore.getInstance();
        authService = new AuthService();

        rvClasses = findViewById(R.id.rv_classes);
        rvClasses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClassAdapter(classList);
        rvClasses.setAdapter(adapter);

        if (findViewById(R.id.btn_back) != null) {
            findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        }
        
        findViewById(R.id.btn_add_class).setOnClickListener(v -> {
            startActivity(new Intent(this, CreateClassActivity.class));
        });

        // Bottom Navigation
        findViewById(R.id.nav_home).setOnClickListener(v -> finish());
        findViewById(R.id.nav_assignments).setOnClickListener(v -> {
            startActivity(new Intent(this, AssignmentManagementActivity.class));
            finish();
        });
        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            startActivity(new Intent(this, TeacherProfileActivity.class));
            finish();
        });

        loadClasses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClasses();
    }

    private void loadClasses() {
        String teacherId = authService.getCurrentUser() != null ? authService.getCurrentUser().getUid() : "";
        db.collection("classes")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    classList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Map<String, Object> data = doc.getData();
                        data.put("id", doc.getId());
                        classList.add(data);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void deleteClass(String classId, String className) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa lớp học")
                .setMessage("Bạn có chắc chắn muốn xóa lớp '" + className + "' không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("classes").document(classId).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Đã xóa lớp " + className, Toast.LENGTH_SHORT).show();
                                loadClasses();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ViewHolder> {
        private List<Map<String, Object>> list;
        public ClassAdapter(List<Map<String, Object>> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> item = list.get(position);
            String classId = String.valueOf(item.get("id"));
            String className = String.valueOf(item.get("className"));
            
            holder.tvName.setText(className);
            holder.tvCode.setText("Mã: " + (item.get("joinCode") != null ? item.get("joinCode") : "N/A"));

            // Đếm sĩ số thực tế
            db.collection("class_members")
                    .whereEqualTo("classId", classId)
                    .get()
                    .addOnSuccessListener(snap -> {
                        int realCount = snap.size();
                        holder.tvCount.setText("Sĩ số: " + realCount + " bé");
                    });

            int[] colors = {0xFFFFD54F, 0xFF81D4FA, 0xFFF48FB1};
            View container = holder.itemView.findViewById(R.id.layout_container);
            if (container != null) {
                container.setBackgroundTintList(android.content.res.ColorStateList.valueOf(colors[position % colors.length]));
            }

            // Xử lý nút xóa
            if (holder.btnDelete != null) {
                holder.btnDelete.setOnClickListener(v -> deleteClass(classId, className));
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ClassManagementActivity.this, ClassDetailActivity.class);
                intent.putExtra("class_id", classId);
                intent.putExtra("class_name", className);
                intent.putExtra("join_code", String.valueOf(item.get("joinCode")));
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvCount, tvCode;
            ImageButton btnDelete;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_class_name);
                tvCount = itemView.findViewById(R.id.tv_student_count);
                tvCode = itemView.findViewById(R.id.tv_join_code_display);
                btnDelete = itemView.findViewById(R.id.btn_delete_class);
            }
        }
    }
}

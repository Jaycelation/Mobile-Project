package com.example.kid_app.teacher;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

        // Chuc nang: khoi tao Firestore de doc ghi du lieu cloud cho man hinh.
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
        View navHome = findViewById(R.id.nav_home);
        if (navHome != null) navHome.setOnClickListener(v -> finish());
        
        View navAssignments = findViewById(R.id.nav_assignments);
        if (navAssignments != null) {
            navAssignments.setOnClickListener(v -> {
                startActivity(new Intent(this, AssignmentManagementActivity.class));
                finish();
            });
        }
        
        View navProfile = findViewById(R.id.nav_profile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, TeacherProfileActivity.class));
                finish();
            });
        }

        loadClasses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClasses();
    }

    private void loadClasses() {
        String teacherId = authService.getCurrentUser() != null ? authService.getCurrentUser().getUid() : "";
        // Chuc nang: goi Firestore de doc hoac ghi du lieu cho chuc nang hien tai.
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
                    // Chuc nang: goi Firestore de doc hoac ghi du lieu cho chuc nang hien tai.
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

            // Gán icon tương ứng (Gấu/Mèo/Thỏ) dựa trên vị trí hoặc dữ liệu
            if (position % 3 == 0) holder.ivIcon.setImageResource(R.drawable.icon_gau);
            else if (position % 3 == 1) holder.ivIcon.setImageResource(R.drawable.icon_meo);
            else holder.ivIcon.setImageResource(R.drawable.icon_tho);

            // Đếm sĩ số thực tế
            // Chuc nang: goi Firestore de doc hoac ghi du lieu cho chuc nang hien tai.
            db.collection("class_members")
                    .whereEqualTo("classId", classId)
                    .get()
                    .addOnSuccessListener(snap -> {
                        int realCount = snap.size();
                        holder.tvCount.setText("Sĩ số: " + realCount + " bé");
                        
                        // Cập nhật progress bar demo (ví dụ mục tiêu là 30 bé)
                        int progress = Math.min((realCount * 100) / 30, 100);
                        holder.progressBar.setProgress(progress);
                        holder.tvPercent.setText(progress + "%");
                    });

            // Xử lý nút xóa
            if (holder.btnDelete != null) {
                holder.btnDelete.setOnClickListener(v -> deleteClass(classId, className));
            }

            // Xử lý các nút bấm mới
            View.OnClickListener detailClick = v -> {
                Intent intent = new Intent(ClassManagementActivity.this, ClassDetailActivity.class);
                intent.putExtra("class_id", classId);
                intent.putExtra("class_name", className);
                intent.putExtra("join_code", String.valueOf(item.get("joinCode")));
                startActivity(intent);
            };

            holder.itemView.setOnClickListener(detailClick);
            if (holder.btnViewDetails != null) holder.btnViewDetails.setOnClickListener(detailClick);
            if (holder.btnManage != null) holder.btnManage.setOnClickListener(detailClick);
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvCount, tvPercent;
            ImageView ivIcon;
            ProgressBar progressBar;
            ImageButton btnDelete;
            View btnViewDetails, btnManage;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_class_name);
                tvCount = itemView.findViewById(R.id.tv_student_count);
                tvPercent = itemView.findViewById(R.id.tv_progress_percent);
                ivIcon = itemView.findViewById(R.id.iv_class_icon);
                progressBar = itemView.findViewById(R.id.pb_class_progress);
                btnDelete = itemView.findViewById(R.id.btn_delete_class);
                btnViewDetails = itemView.findViewById(R.id.btn_view_details);
                btnManage = itemView.findViewById(R.id.btn_manage);
            }
        }
    }
}

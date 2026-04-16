package com.example.kid_app.teacher;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kid_app.R;
import com.example.kid_app.auth.AuthService;
import com.example.kid_app.common.BaseActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssignmentManagementActivity extends BaseActivity {

    private RecyclerView rvAssignments;
    private AssignmentAdapter adapter;
    private List<Map<String, Object>> assignmentList = new ArrayList<>();
    private List<Map<String, Object>> filteredList = new ArrayList<>();
    private List<Map<String, String>> classList = new ArrayList<>();
    
    private FirebaseFirestore db;
    private AuthService authService;
    private EditText etSearch;
    private TextView tvSelectedClass;
    private String selectedClassId = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_management);

        db = FirebaseFirestore.getInstance();
        authService = new AuthService();

        bindViews();
        loadClasses();
        loadAssignments();
    }

    private void bindViews() {
        etSearch = findViewById(R.id.et_search_assignment);
        tvSelectedClass = findViewById(R.id.tv_selected_class_name);
        rvAssignments = findViewById(R.id.rv_assignments);
        rvAssignments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AssignmentAdapter(filteredList);
        rvAssignments.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_assignment).setOnClickListener(v -> startActivity(new Intent(this, CreateAssignmentActivity.class)));

        findViewById(R.id.layout_select_class).setOnClickListener(v -> showClassSelector());

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { filter(); }
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        View navHome = findViewById(R.id.nav_home);
        if (navHome != null) navHome.setOnClickListener(v -> finish());

        View navClasses = findViewById(R.id.nav_classes);
        if (navClasses != null) navClasses.setOnClickListener(v -> {
            startActivity(new Intent(this, ClassManagementActivity.class));
            finish();
        });

        View navProfile = findViewById(R.id.nav_profile);
        if (navProfile != null) navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, TeacherProfileActivity.class));
            finish();
        });
    }

    private void loadClasses() {
        String teacherId = authService.getCurrentUser() != null ? authService.getCurrentUser().getUid() : "";
        db.collection("classes")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(snap -> {
                    classList.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        classList.add(Map.of("id", doc.getId(), "name", doc.getString("className")));
                    }
                });
    }

    private void showClassSelector() {
        String[] names = new String[classList.size() + 1];
        names[0] = "Tất cả các lớp";
        for (int i = 0; i < classList.size(); i++) names[i + 1] = classList.get(i).get("name");

        new AlertDialog.Builder(this)
                .setTitle("Chọn lớp học")
                .setItems(names, (dialog, which) -> {
                    if (which == 0) {
                        selectedClassId = "all";
                        tvSelectedClass.setText("Tất cả các lớp");
                    } else {
                        selectedClassId = classList.get(which - 1).get("id");
                        tvSelectedClass.setText(classList.get(which - 1).get("name"));
                    }
                    loadAssignments();
                }).show();
    }

    private void loadAssignments() {
        String teacherId = authService.getCurrentUser() != null ? authService.getCurrentUser().getUid() : "";
        var query = db.collection("assignments").whereEqualTo("teacherId", teacherId);
        
        if (!selectedClassId.equals("all")) {
            query = query.whereEqualTo("classId", selectedClassId);
        }

        query.get().addOnSuccessListener(snap -> {
            assignmentList.clear();
            for (QueryDocumentSnapshot doc : snap) {
                Map<String, Object> data = doc.getData();
                data.put("id", doc.getId());
                assignmentList.add(data);
            }
            filter();
        });
    }

    private void filter() {
        filteredList.clear();
        String query = etSearch.getText().toString().toLowerCase().trim();
        for (Map<String, Object> item : assignmentList) {
            if (query.isEmpty() || String.valueOf(item.get("title")).toLowerCase().contains(query)) {
                filteredList.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void deleteAssignment(String assignmentId, String title) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa bài tập")
                .setMessage("Bạn có chắc chắn muốn xóa bài tập '" + title + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("assignments").document(assignmentId).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Đã xóa bài tập!", Toast.LENGTH_SHORT).show();
                                loadAssignments();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.ViewHolder> {
        private List<Map<String, Object>> list;
        public AssignmentAdapter(List<Map<String, Object>> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assignment_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> item = list.get(position);
            String id = String.valueOf(item.get("id"));
            String title = String.valueOf(item.get("title"));
            
            holder.tvTitle.setText(title);
            holder.tvClass.setText(String.valueOf(item.get("className")));
            holder.tvDate.setText("Hạn: " + item.get("dueDate"));
            
            if (holder.btnDelete != null) {
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.btnDelete.setOnClickListener(v -> deleteAssignment(id, title));
            }

            holder.btnDetails.setOnClickListener(v -> {
                Intent intent = new Intent(AssignmentManagementActivity.this, AssignmentDetailActivity.class);
                intent.putExtra("assignment_id", id);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvClass, tvDate;
            MaterialButton btnDetails;
            ImageButton btnDelete;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_assignment_title);
                tvClass = itemView.findViewById(R.id.tv_class_tag);
                tvDate = itemView.findViewById(R.id.tv_due_date);
                btnDetails = itemView.findViewById(R.id.btn_details);
                btnDelete = itemView.findViewById(R.id.btn_delete_assignment);
            }
        }
    }
}

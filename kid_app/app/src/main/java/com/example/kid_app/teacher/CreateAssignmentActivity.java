package com.example.kid_app.teacher;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.kid_app.R;
import com.example.kid_app.auth.AuthService;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateAssignmentActivity extends BaseActivity {

    private Spinner spinnerClass, spinnerActivityType;
    private EditText etTitle, etDesc, etDueDate;
    private FirebaseFirestore db;
    private AuthService authService;
    private List<String> classIds = new ArrayList<>();
    private List<String> classNames = new ArrayList<>();

    // Danh sách 10 trò chơi thực hành
    private final String[] practiceGames = {
            "Thử thách con số",
            "Đếm số quả",
            "Ghép hình bóng",
            "Đúng con vật",
            "Đúng chữ cái",
            "Đúng loại quả",
            "Đúng màu sắc",
            "Quy luật logic",
            "Nhanh tay tinh mắt",
            "Ghép tranh trí tuệ"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_assignment);

        db = FirebaseFirestore.getInstance();
        authService = new AuthService();

        bindViews();
        setupActivityTypeSpinner();
        loadClasses();
    }

    private void bindViews() {
        spinnerClass = findViewById(R.id.spinner_class);
        spinnerActivityType = findViewById(R.id.spinner_activity_type);
        etTitle = findViewById(R.id.et_title);
        etDesc = findViewById(R.id.et_desc);
        etDueDate = findViewById(R.id.et_due_date);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        etDueDate.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.btn_assign).setOnClickListener(v -> saveAssignment());
    }

    private void setupActivityTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, practiceGames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivityType.setAdapter(adapter);
    }

    private void loadClasses() {
        String teacherId = authService.getCurrentUser() != null ? authService.getCurrentUser().getUid() : "";
        
        db.collection(AppConstants.COL_CLASSES)
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    classNames.clear();
                    classIds.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        classNames.add(doc.getString("className"));
                        classIds.add(doc.getId());
                    }
                    
                    if (classNames.isEmpty()) {
                        classNames.add("Chưa có lớp học");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerClass.setAdapter(adapter);
                });
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> 
                        etDueDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", monthOfYear + 1, dayOfMonth, year1)), 
                year, month, day);
        datePickerDialog.show();
    }

    private void saveAssignment() {
        String title = etTitle.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String dueDate = etDueDate.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Nhập tiêu đề");
            return;
        }

        if (classIds.isEmpty()) {
            Toast.makeText(this, "Bạn cần tạo lớp học trước!", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedClassIndex = spinnerClass.getSelectedItemPosition();
        String classId = classIds.get(selectedClassIndex);
        String className = classNames.get(selectedClassIndex);
        String gameName = spinnerActivityType.getSelectedItem().toString();

        Map<String, Object> assignment = new HashMap<>();
        assignment.put("title", title);
        assignment.put("description", desc);
        assignment.put("dueDate", dueDate);
        assignment.put("classId", classId);
        assignment.put("className", className);
        assignment.put("gameType", gameName); // Lưu tên trò chơi để bé làm đúng trò đó
        assignment.put("questionCount", 5);   // Cố định 5 câu theo yêu cầu
        assignment.put("teacherId", authService.getCurrentUser().getUid());
        assignment.put("createdAt", com.google.firebase.Timestamp.now());
        assignment.put("status", "active");

        db.collection(AppConstants.COL_ASSIGNMENTS).add(assignment)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Đã giao bài tập: " + gameName + " (5 câu) thành công! 📝", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

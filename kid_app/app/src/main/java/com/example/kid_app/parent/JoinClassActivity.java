package com.example.kid_app.parent;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.example.kid_app.R;
import com.example.kid_app.auth.AuthService;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class JoinClassActivity extends BaseActivity {

    private EditText etJoinCode;
    private FirebaseFirestore db;
    private String childId, childName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_class);

        db = FirebaseFirestore.getInstance();
        childId = getIntent().getStringExtra(AppConstants.KEY_CHILD_ID);
        childName = getIntent().getStringExtra("child_name"); // Nhận tên bé từ Intent

        if (childId == null) {
            Toast.makeText(this, "Không tìm thấy hồ sơ bé!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etJoinCode = findViewById(R.id.et_join_code);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_join).setOnClickListener(v -> validateAndJoin());
    }

    private void validateAndJoin() {
        String code = etJoinCode.getText().toString().trim();
        if (code.isEmpty()) {
            etJoinCode.setError("Vui lòng nhập mã");
            return;
        }

        db.collection("classes")
                .whereEqualTo("joinCode", code)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "Mã lớp không tồn tại!", Toast.LENGTH_SHORT).show();
                    } else {
                        String classId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        String className = queryDocumentSnapshots.getDocuments().get(0).getString("className");
                        checkAndAddMember(classId, className);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void checkAndAddMember(String classId, String className) {
        db.collection("class_members")
                .whereEqualTo("classId", classId)
                .whereEqualTo("childId", childId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(this, "Bé đã tham gia lớp này rồi!", Toast.LENGTH_SHORT).show();
                    } else {
                        addNewMember(classId, className);
                    }
                });
    }

    private void addNewMember(String classId, String className) {
        Map<String, Object> member = new HashMap<>();
        member.put("classId", classId);
        member.put("childId", childId);
        member.put("studentName", childName); // LƯU TÊN BÉ VÀO ĐÂY ĐỂ CÔ GIÁO LOAD NHANH
        member.put("className", className);
        member.put("joinedAt", com.google.firebase.Timestamp.now());
        member.put("memberStatus", "active");

        db.collection("class_members").add(member)
                .addOnSuccessListener(ref -> {
                    db.collection("classes").document(classId)
                            .update("studentCount", FieldValue.increment(1));

                    db.collection(AppConstants.COL_CHILD_PROFILES).document(childId)
                            .update("currentClassId", classId, "classId", classId, "className", className);

                    Toast.makeText(this, "Chúc mừng! Bé đã tham gia " + className + " 🎉", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi tham gia: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

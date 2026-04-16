package com.example.kid_app.teacher;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.example.kid_app.R;
import com.example.kid_app.auth.AuthService;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.mapper.DocumentMapper;
import com.example.kid_app.data.model.Account;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditTeacherProfileActivity extends BaseActivity {

    private EditText etFullName, etPhone;
    private FirebaseFirestore db;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_teacher_profile);

        db = FirebaseFirestore.getInstance();
        authService = new AuthService();

        bindViews();
        loadCurrentInfo();
    }

    private void bindViews() {
        etFullName = findViewById(R.id.et_full_name);
        etPhone = findViewById(R.id.et_phone);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save).setOnClickListener(v -> saveProfile());
    }

    private void loadCurrentInfo() {
        authService.getCurrentUserAccount()
                .addOnSuccessListener(doc -> {
                    Account account = DocumentMapper.toAccount(doc);
                    if (account != null) {
                        etFullName.setText(account.getFullName());
                        // Giả sử phone được lưu trong doc
                        if (doc.contains("phone")) etPhone.setText(doc.getString("phone"));
                    }
                });
    }

    private void saveProfile() {
        String name = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            etFullName.setError("Nhập tên");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", name);
        updates.put("phone", phone);

        String uid = authService.getCurrentUser().getUid();
        db.collection("accounts").document(uid).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thành công! ✨", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

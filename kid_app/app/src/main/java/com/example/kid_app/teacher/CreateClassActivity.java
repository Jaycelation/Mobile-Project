package com.example.kid_app.teacher;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kid_app.R;
import com.example.kid_app.auth.AuthService;
import com.example.kid_app.common.BaseActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CreateClassActivity extends BaseActivity {

    private EditText etClassName, etClassDesc;
    private TextView tvJoinCode;
    private String generatedCode;
    private FirebaseFirestore db;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_class);

        db = FirebaseFirestore.getInstance();
        authService = new AuthService();

        bindViews();
        generateRandomCode();
    }

    private void bindViews() {
        etClassName = findViewById(R.id.et_class_name);
        etClassDesc = findViewById(R.id.et_class_desc);
        tvJoinCode = findViewById(R.id.tv_join_code);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_copy).setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Join Code", generatedCode);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Đã sao chép mã!", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_complete).setOnClickListener(v -> createClass());
    }

    private void generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        generatedCode = sb.toString();
        tvJoinCode.setText(generatedCode);
    }

    private void createClass() {
        String name = etClassName.getText().toString().trim();
        String desc = etClassDesc.getText().toString().trim();

        if (name.isEmpty()) {
            etClassName.setError("Vui lòng nhập tên lớp");
            return;
        }

        Map<String, Object> clazz = new HashMap<>();
        clazz.put("className", name);
        clazz.put("description", desc);
        clazz.put("joinCode", generatedCode);
        clazz.put("teacherId", authService.getCurrentUser() != null ? authService.getCurrentUser().getUid() : "unknown");
        clazz.put("studentCount", 0);
        clazz.put("status", "active");
        clazz.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("classes").add(clazz)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Tạo lớp thành công! 🎉", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

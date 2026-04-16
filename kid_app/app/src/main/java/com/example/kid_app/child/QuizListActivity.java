package com.example.kid_app.child;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class QuizListActivity extends BaseActivity {

    private String assignmentId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_list);

        db = FirebaseFirestore.getInstance();
        assignmentId = getIntent().getStringExtra(AppConstants.KEY_ASSIGNMENT_ID);

        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        TextView tvTitle = findViewById(R.id.tv_quiz_title);
        if (tvTitle != null) tvTitle.setText("Thử thách đố vui");

        TextView tvDesc = findViewById(R.id.tv_quiz_desc);
        if (tvDesc != null) tvDesc.setText("Trả lời đúng các câu hỏi để nhận huy hiệu Thông Thái nhé!");

        View btnStart = findViewById(R.id.btn_start_quiz);
        if (btnStart != null) {
            btnStart.setOnClickListener(v -> checkAndStartQuiz());
        }

        View btnHistory = findViewById(R.id.btn_history);
        if (btnHistory != null) {
            btnHistory.setOnClickListener(v -> {
                startActivity(new Intent(this, QuizHistoryActivity.class));
            });
        }
    }

    private void checkAndStartQuiz() {
        // 🔥 LOGIC MỚI: Kiểm tra database thật
        // Nếu là bài tập, dùng assignmentId. Nếu không, mặc định dùng quiz_animals_01 của em
        String contentId = (assignmentId != null) ? assignmentId : "quiz_animals_01";

        db.collection("content_catalog").document(contentId).collection("questions")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // Nếu ID bài tập chưa có câu hỏi, thử dùng bộ quiz_animals_01 làm mặc định
                        if (assignmentId != null) {
                            startQuizActivity("quiz_animals_01");
                        } else {
                            Toast.makeText(this, "Chưa có câu hỏi cho nội dung này!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        startQuizActivity(contentId);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi kết nối database!", Toast.LENGTH_SHORT).show();
                });
    }

    private void startQuizActivity(String contentId) {
        Intent intent = new Intent(this, QuizPlayActivity.class);
        intent.putExtra(AppConstants.KEY_CONTENT_ID, contentId);
        intent.putExtra(AppConstants.KEY_ASSIGNMENT_ID, assignmentId);
        startActivity(intent);
        finish();
    }
}

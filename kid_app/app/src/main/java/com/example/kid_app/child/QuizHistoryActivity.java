package com.example.kid_app.child;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.model.ActivityAttempt;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class QuizHistoryActivity extends BaseActivity {

    private RecyclerView rvHistory;
    private TextView tvEmpty;
    private QuizHistoryAdapter adapter;
    private List<ActivityAttempt> attempts = new ArrayList<>();
    private String childId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_history);

        rvHistory = findViewById(R.id.rv_history);
        tvEmpty = findViewById(R.id.tv_empty);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuizHistoryAdapter(attempts);
        rvHistory.setAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        childId = prefs.getString(AppConstants.PREF_SELECTED_CHILD_ID, null);

        if (childId == null) {
            Toast.makeText(this, "Không tìm thấy hồ sơ bé", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadHistory();
    }

    private void loadHistory() {
        FirebaseFirestore.getInstance()
                .collection(AppConstants.COL_CHILD_PROFILES)
                .document(childId)
                .collection(AppConstants.SUBCOL_ACTIVITY_ATTEMPTS)
                .whereEqualTo("contentType", "quiz")
                .orderBy("startedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    attempts.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ActivityAttempt attempt = doc.toObject(ActivityAttempt.class);
                        attempt.setAttemptId(doc.getId());
                        attempts.add(attempt);
                    }
                    adapter.notifyDataSetChanged();
                    
                    if (attempts.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvHistory.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvHistory.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải lịch sử: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

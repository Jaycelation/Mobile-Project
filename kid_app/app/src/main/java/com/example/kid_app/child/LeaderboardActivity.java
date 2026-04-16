package com.example.kid_app.child;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LeaderboardActivity extends BaseActivity {

    private RecyclerView rvLeaderboard;
    private ProgressBar progressBar;
    private TextView tvClassName;
    
    private LeaderboardAdapter adapter;
    private List<StudentRank> rankList = new ArrayList<>();
    
    private FirebaseFirestore db;
    private String selectedChildId;
    private String currentClassId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        selectedChildId = prefs.getString(AppConstants.PREF_SELECTED_CHILD_ID, null);

        bindViews();
        loadChildClass();
    }

    private void bindViews() {
        progressBar = findViewById(R.id.progressBar);
        tvClassName = findViewById(R.id.tv_class_name);
        rvLeaderboard = findViewById(R.id.rv_leaderboard);
        
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter(rankList);
        rvLeaderboard.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void loadChildClass() {
        if (selectedChildId == null) return;
        progressBar.setVisibility(View.VISIBLE);
        
        db.collection(AppConstants.COL_CHILD_PROFILES).document(selectedChildId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    currentClassId = doc.getString("currentClassId");
                    if (currentClassId == null || currentClassId.isEmpty()) {
                        currentClassId = doc.getString("classId");
                    }
                    if (currentClassId != null && !currentClassId.isEmpty()) {
                        loadClassName();
                        loadLeaderboard();
                    } else {
                        tvClassName.setText("Bé chưa tham gia lớp học nào");
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
    }

    private void loadClassName() {
        db.collection(AppConstants.COL_CLASSES).document(currentClassId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    tvClassName.setText(doc.getString("className"));
                }
            });
    }

    private void loadLeaderboard() {
        db.collection(AppConstants.COL_CLASS_MEMBERS)
            .whereEqualTo("classId", currentClassId)
            .get()
            .addOnSuccessListener(querySnap -> {
                if (querySnap.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                
                final int totalStudents = querySnap.size();
                AtomicInteger count = new AtomicInteger(0);
                
                for (DocumentSnapshot memberDoc : querySnap.getDocuments()) {
                    String childId = memberDoc.getString("childId");
                    if (childId == null) continue;
                    
                    fetchStudentData(childId, totalStudents, count);
                }
            });
    }

    private void fetchStudentData(String childId, int totalStudents, AtomicInteger count) {
        db.collection(AppConstants.COL_CHILD_PROFILES).document(childId).get()
            .addOnSuccessListener(profileDoc -> {
                String name = profileDoc.getString("name");
                if (name == null || name.isEmpty()) name = profileDoc.getString("fullName");
                if (name == null || name.isEmpty()) name = "Bé ngoan";
                
                final String finalName = name;
                
                db.collection(AppConstants.COL_CHILD_STATS).document(childId).get()
                    .addOnCompleteListener(statsTask -> {
                        long points = 0;
                        if (statsTask.isSuccessful() && statsTask.getResult().exists()) {
                            Long p = statsTask.getResult().getLong("totalPoints");
                            if (p != null) points = p;
                        }
                        
                        rankList.add(new StudentRank(childId, finalName, points));
                        
                        if (count.incrementAndGet() == totalStudents) {
                            sortAndShowLeaderboard();
                        }
                    });
            });
    }

    private void sortAndShowLeaderboard() {
        Collections.sort(rankList, (a, b) -> Long.compare(b.points, a.points)); // Giảm dần
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    // --- INNER CLASSES ---

    private static class StudentRank {
        String id, name;
        long points;
        StudentRank(String id, String name, long points) {
            this.id = id; this.name = name; this.points = points;
        }
    }

    private class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
        private List<StudentRank> list;
        LeaderboardAdapter(List<StudentRank> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            StudentRank item = list.get(position);
            int rank = position + 1;
            
            holder.tvRank.setText(String.valueOf(rank));
            holder.tvName.setText(item.name);
            holder.tvPoints.setText(item.points + " điểm");
            
            // Đánh dấu bản thân
            if (item.id.equals(selectedChildId)) {
                holder.itemView.setBackgroundColor(Color.parseColor("#E8F5E9")); // Xanh lá mờ
                holder.tvName.setText(item.name + " (Bạn)");
            } else {
                holder.itemView.setBackgroundColor(Color.WHITE);
            }
            
            // Huy hiệu cho TOP 3
            holder.tvBadge.setVisibility(View.VISIBLE);
            if (rank == 1) {
                holder.tvBadge.setText("🏆");
                holder.tvRank.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFD700"))); // Vàng
                holder.tvRank.setTextColor(Color.WHITE);
            } else if (rank == 2) {
                holder.tvBadge.setText("🥈");
                holder.tvRank.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#C0C0C0"))); // Bạc
                holder.tvRank.setTextColor(Color.WHITE);
            } else if (rank == 3) {
                holder.tvBadge.setText("🥉");
                holder.tvRank.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#CD7F32"))); // Đồng
                holder.tvRank.setTextColor(Color.WHITE);
            } else {
                holder.tvBadge.setVisibility(View.INVISIBLE);
                holder.tvRank.setBackgroundTintList(null);
                holder.tvRank.setTextColor(getResources().getColor(R.color.primary_green_dark));
            }
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvRank, tvName, tvPoints, tvBadge;
            ViewHolder(View v) {
                super(v);
                tvRank = v.findViewById(R.id.tv_rank);
                tvName = v.findViewById(R.id.tv_child_name);
                tvPoints = v.findViewById(R.id.tv_points);
                tvBadge = v.findViewById(R.id.tv_badge_icon);
            }
        }
    }
}

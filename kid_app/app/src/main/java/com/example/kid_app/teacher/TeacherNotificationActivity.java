package com.example.kid_app.teacher;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kid_app.R;
import com.example.kid_app.auth.AuthService;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TeacherNotificationActivity extends BaseActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<Map<String, Object>> notificationList = new ArrayList<>();
    private FirebaseFirestore db;
    private AuthService authService;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_notifications);

        // Chuc nang: khoi tao Firestore de doc ghi du lieu cloud cho man hinh.
        db = FirebaseFirestore.getInstance();
        authService = new AuthService();

        rvNotifications = findViewById(R.id.rv_notifications);
        tvEmpty = findViewById(R.id.tv_empty);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notificationList);
        rvNotifications.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadNotifications();
    }

    private void loadNotifications() {
        String teacherId = authService.getCurrentUser() != null ? authService.getCurrentUser().getUid() : "";
        if (teacherId.isEmpty()) return;

        // Chuc nang: goi Firestore de doc hoac ghi du lieu cho chuc nang hien tai.
        db.collection(AppConstants.COL_ASSIGNMENTS)
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(assignmentSnap -> {
                    notificationList.clear();
                    if (assignmentSnap.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    List<String> assignmentIds = new ArrayList<>();
                    for (DocumentSnapshot doc : assignmentSnap) {
                        assignmentIds.add(doc.getId());
                        checkDeadline(doc);
                    }

                    // 1. Lấy thông báo nộp bài
                    // Chuc nang: goi Firestore de doc hoac ghi du lieu cho chuc nang hien tai.
                    db.collection(AppConstants.COL_ASSIGNMENT_SUBMISSIONS)
                            .whereIn("assignmentId", assignmentIds)
                            .orderBy("completedAt", Query.Direction.DESCENDING)
                            .limit(10)
                            .get()
                            .addOnSuccessListener(subSnaps -> {
                                for (DocumentSnapshot doc : subSnaps) {
                                    Map<String, Object> notif = new HashMap<>(doc.getData());
                                    notif.put("notifType", "SUBMISSION");
                                    notif.put("time", doc.getTimestamp("completedAt"));
                                    notificationList.add(notif);
                                }
                                sortAndNotify();
                            });

                    // 2. Lấy thông báo Huy hiệu thực tế (Chăm ngoan, Họa sĩ nhí...)
                    loadBadgeNotifications();
                });
    }

    private void checkDeadline(DocumentSnapshot doc) {
        String dueDateStr = doc.getString("dueDate");
        if (dueDateStr == null) return;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date dueDate = sdf.parse(dueDateStr);
            long diff = dueDate.getTime() - System.currentTimeMillis();
            long days = diff / (24 * 60 * 60 * 1000);
            
            if (days >= 0 && days <= 2) {
                Map<String, Object> notif = new HashMap<>();
                notif.put("notifType", "DEADLINE");
                notif.put("title", doc.getString("title"));
                notif.put("daysLeft", days);
                notif.put("time", new Timestamp(new Date()));
                notificationList.add(notif);
            }
        } catch (Exception ignored) {}
    }

    private void loadBadgeNotifications() {
        // Lấy các huy hiệu mới nhất được trao trong hệ thống
        // Chuc nang: goi Firestore de doc hoac ghi du lieu cho chuc nang hien tai.
        db.collection(AppConstants.COL_CHILD_BADGES)
                .orderBy("awardedAt", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(snaps -> {
                    for (DocumentSnapshot doc : snaps) {
                        Map<String, Object> notif = new HashMap<>(doc.getData());
                        notif.put("notifType", "BADGE_AWARDED");
                        notif.put("time", doc.getTimestamp("awardedAt"));
                        notificationList.add(notif);
                    }
                    sortAndNotify();
                });
    }

    private void sortAndNotify() {
        Collections.sort(notificationList, (o1, o2) -> {
            Timestamp t1 = (Timestamp) o1.get("time");
            Timestamp t2 = (Timestamp) o2.get("time");
            if (t1 == null || t2 == null) return 0;
            return t2.compareTo(t1);
        });
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(notificationList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        private List<Map<String, Object>> list;
        private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());

        public NotificationAdapter(List<Map<String, Object>> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assignment_card, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> item = list.get(position);
            String type = (String) item.get("notifType");
            Timestamp ts = (Timestamp) item.get("time");
            holder.tvDate.setText(ts != null ? timeFormat.format(ts.toDate()) : "");

            if ("SUBMISSION".equals(type)) {
                holder.tvType.setText("Bé vừa nộp bài");
                loadSubmissionDetails(holder, item);
            } else if ("DEADLINE".equals(type)) {
                holder.tvType.setText("⚠️ Sắp hết hạn");
                holder.tvTitle.setText("Bài tập: " + item.get("title"));
                long days = (long) item.get("daysLeft");
                holder.tvDate.setText(days == 0 ? "Hết hạn hôm nay!" : "Còn " + days + " ngày nữa");
            } else if ("BADGE_AWARDED".equals(type)) {
                holder.tvType.setText("🎉 Vinh danh bé");
                loadBadgeAwardDetails(holder, item);
            }
        }

        private void loadSubmissionDetails(ViewHolder holder, Map<String, Object> item) {
            // Chuc nang: goi Firestore de doc hoac ghi du lieu cho chuc nang hien tai.
            db.collection(AppConstants.COL_CHILD_PROFILES).document((String) item.get("childId")).get().addOnSuccessListener(d -> {
                if (d.exists()) holder.tvTitle.setText(d.getString("fullName") + " đã nộp bài");
            });
        }

        private void loadBadgeAwardDetails(ViewHolder holder, Map<String, Object> item) {
            String badgeId = (String) item.get("badgeId");
            String childId = (String) item.get("childId");
            
            // Lấy tên huy hiệu
            String badgeName = "Huy hiệu mới";
            if ("badge_chăm ngoan".equals(badgeId) || "badge_star".equals(badgeId)) badgeName = "Chăm ngoan ⭐";
            else if ("badge_họa sĩ nhí".equals(badgeId) || "badge_painter".equals(badgeId)) badgeName = "Họa sĩ nhí 🎨";
            
            final String finalBadgeName = badgeName;
            // Chuc nang: goi Firestore de doc hoac ghi du lieu cho chuc nang hien tai.
            db.collection(AppConstants.COL_CHILD_PROFILES).document(childId).get().addOnSuccessListener(d -> {
                if (d.exists()) holder.tvTitle.setText(d.getString("fullName") + " nhận huy hiệu " + finalBadgeName);
            });
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvType, tvDate;
            public ViewHolder(@NonNull View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_assignment_title);
                tvType = v.findViewById(R.id.tv_class_tag);
                tvDate = v.findViewById(R.id.tv_due_date);
                if (v.findViewById(R.id.btn_details) != null) v.findViewById(R.id.btn_details).setVisibility(View.GONE);
                if (v.findViewById(R.id.pb_completion) != null) v.findViewById(R.id.pb_completion).setVisibility(View.GONE);
            }
        }
    }
}

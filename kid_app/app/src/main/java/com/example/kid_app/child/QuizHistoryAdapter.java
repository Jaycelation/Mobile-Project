package com.example.kid_app.child;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kid_app.R;
import com.example.kid_app.data.model.ActivityAttempt;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class QuizHistoryAdapter extends RecyclerView.Adapter<QuizHistoryAdapter.ViewHolder> {

    private List<ActivityAttempt> attempts;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public QuizHistoryAdapter(List<ActivityAttempt> attempts) {
        this.attempts = attempts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityAttempt attempt = attempts.get(position);
        
        holder.tvTitle.setText("Chủ đề: " + (attempt.getContentId() != null ? attempt.getContentId() : "N/A"));
        
        if (attempt.getStartedAt() != null) {
            holder.tvDate.setText("Ngày làm: " + dateFormat.format(attempt.getStartedAt()));
        } else {
            holder.tvDate.setText("Ngày làm: N/A");
        }
        
        holder.tvTime.setText("Thời gian: " + attempt.getDurationSeconds() + "s");
        holder.tvScore.setText(String.valueOf(attempt.getScore()));
        
        if (attempt.isPassed()) {
            holder.tvStatus.setText("Hoàn thành");
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // green
        } else {
            holder.tvStatus.setText("Đang làm");
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#F44336")); // red
        }
    }

    @Override
    public int getItemCount() {
        return attempts != null ? attempts.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvTime, tvScore, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_history_title);
            tvDate = itemView.findViewById(R.id.tv_history_date);
            tvTime = itemView.findViewById(R.id.tv_history_time);
            tvScore = itemView.findViewById(R.id.tv_history_score);
            tvStatus = itemView.findViewById(R.id.tv_history_status);
        }
    }
}

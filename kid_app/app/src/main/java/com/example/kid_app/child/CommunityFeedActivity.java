package com.example.kid_app.child;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kid_app.R;
import com.example.kid_app.auth.AuthService;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityFeedActivity extends BaseActivity {

    private RecyclerView rvFeed;
    private FeedAdapter adapter;
    private List<Map<String, Object>> feedList = new ArrayList<>();
    private FirebaseFirestore db;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_feed);

        db = FirebaseFirestore.getInstance();
        authService = new AuthService();
        rvFeed = findViewById(R.id.rv_feed);
        
        if (rvFeed != null) {
            rvFeed.setLayoutManager(new LinearLayoutManager(this));
            adapter = new FeedAdapter(feedList);
            rvFeed.setAdapter(adapter);
        }

        bindBottomNavigation();
        loadPostsFromFirestore();

        View cardPostInput = findViewById(R.id.card_post_input);
        if (cardPostInput != null) {
            cardPostInput.setOnClickListener(v -> {
                startActivity(new Intent(this, CreatePostActivity.class));
            });
        }
    }

    private void bindBottomNavigation() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.nav_home).setOnClickListener(v -> finish());
        findViewById(R.id.nav_progress).setOnClickListener(v -> {
            startActivity(new Intent(this, ChildProgressActivity.class));
            finish();
        });
        findViewById(R.id.nav_profile).setOnClickListener(v -> {
            startActivity(new Intent(this, ChildProfileActivity.class));
            finish();
        });
    }

    private void loadPostsFromFirestore() {
        db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        feedList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Map<String, Object> post = doc.getData();
                            post.put("id", doc.getId());
                            feedList.add(post);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {
        private List<Map<String, Object>> list;
        public FeedAdapter(List<Map<String, Object>> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> item = list.get(position);
            String postId = (String) item.get("id");
            
            holder.tvName.setText(String.valueOf(item.getOrDefault("authorName", "Ẩn danh")));
            holder.tvContent.setText(String.valueOf(item.getOrDefault("content", "")));
            
            long likes = 0;
            Object likesObj = item.get("likes");
            if (likesObj instanceof Long) likes = (Long) likesObj;
            else if (likesObj instanceof Integer) likes = (Integer) likesObj;
            holder.tvLikeCount.setText("Thích (" + likes + ")");

            holder.ivAvatar.setImageResource(R.drawable.bg_avatar_circle);
            
            String imageUrl = (String) item.get("imageUrl");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                holder.cardPostImage.setVisibility(View.VISIBLE);
                Glide.with(holder.itemView.getContext()).load(imageUrl).centerCrop().into(holder.ivPostImage);
            } else {
                holder.cardPostImage.setVisibility(View.GONE);
            }
            
            // Nút Thích
            holder.layoutLike.setOnClickListener(v -> {
                db.collection("posts").document(postId).update("likes", FieldValue.increment(1));
            });

            // Nút Bình luận
            holder.tvComment.setOnClickListener(v -> showCommentDialog(postId));

            // LOAD BÌNH LUẬN VÀ HIỂN THỊ NGAY LẬP TỨC
            loadCommentsForPost(postId, holder.layoutCommentsContainer);
        }

        private void loadCommentsForPost(String postId, LinearLayout container) {
            db.collection("posts").document(postId).collection("comments")
                    .orderBy("createdAt", Query.Direction.ASCENDING)
                    .addSnapshotListener((value, error) -> {
                        if (value == null) return;
                        container.removeAllViews();
                        if (value.isEmpty()) {
                            container.setVisibility(View.GONE);
                        } else {
                            container.setVisibility(View.VISIBLE);
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                TextView tv = new TextView(CommunityFeedActivity.this);
                                String text = doc.getString("text");
                                String author = "Bạn nhỏ: ";
                                tv.setText(author + text);
                                tv.setPadding(0, 4, 0, 4);
                                tv.setTextColor(Color.parseColor("#555555"));
                                tv.setTextSize(13);
                                container.addView(tv);
                            }
                        }
                    });
        }

        private void showCommentDialog(String postId) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CommunityFeedActivity.this);
            builder.setTitle("Bình luận của bé");
            final EditText input = new EditText(CommunityFeedActivity.this);
            input.setHint("Nhập nội dung...");
            builder.setView(input);
            builder.setPositiveButton("Gửi", (dialog, which) -> {
                String comment = input.getText().toString().trim();
                if (!comment.isEmpty()) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("text", comment);
                    data.put("createdAt", FieldValue.serverTimestamp());
                    db.collection("posts").document(postId).collection("comments").add(data);
                }
            });
            builder.setNegativeButton("Hủy", null);
            builder.show();
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvContent, tvLikeCount, tvComment;
            LinearLayout layoutLike, layoutCommentsContainer;
            ImageView ivAvatar, ivPostImage;
            View cardPostImage;
            public ViewHolder(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_user_name);
                tvContent = v.findViewById(R.id.tv_post_content);
                layoutLike = v.findViewById(R.id.layout_like);
                tvLikeCount = v.findViewById(R.id.tv_like_count);
                tvComment = v.findViewById(R.id.tv_comment);
                ivAvatar = v.findViewById(R.id.iv_user_avatar);
                ivPostImage = v.findViewById(R.id.iv_post_image);
                cardPostImage = v.findViewById(R.id.card_post_image);
                layoutCommentsContainer = v.findViewById(R.id.layout_comments_container);
            }
        }
    }
}

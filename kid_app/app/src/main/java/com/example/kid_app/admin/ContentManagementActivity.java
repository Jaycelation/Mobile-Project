package com.example.kid_app.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.mapper.DocumentMapper;
import com.example.kid_app.data.model.ContentCatalog;
import com.example.kid_app.data.repository.ContentRepository;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContentManagementActivity extends BaseActivity {

    private RecyclerView rvContent;
    private ProgressBar progressBar;
    private ContentAdapter adapter;
    private ContentRepository contentRepository;
    private List<ContentCatalog> contentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_management);

        contentRepository = new ContentRepository();
        
        rvContent = findViewById(R.id.rv_content);
        progressBar = findViewById(R.id.progress_bar);
        
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.fab_add_content).setOnClickListener(v -> showAddContentDialog());

        setupRecyclerView();
        loadContent();
    }

    private void setupRecyclerView() {
        adapter = new ContentAdapter(contentList);
        // Sử dụng GridLayoutManager để hiện thị 2 cột như trong ảnh gợi ý
        rvContent.setLayoutManager(new GridLayoutManager(this, 2));
        rvContent.setAdapter(adapter);
    }

    private void loadContent() {
        progressBar.setVisibility(View.VISIBLE);
        contentRepository.getAllActiveContent()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    contentList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        ContentCatalog content = DocumentMapper.toContentCatalog(doc);
                        if (content != null) {
                            contentList.add(content);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Lỗi tải nội dung: " + e.getMessage());
                });
    }

    private void showAddContentDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_content, null);
        TextInputLayout tilTitle = dialogView.findViewById(R.id.til_content_title);
        TextInputLayout tilDesc = dialogView.findViewById(R.id.til_content_desc);
        Spinner spinnerType = dialogView.findViewById(R.id.spinner_content_type);
        Spinner spinnerAge = dialogView.findViewById(R.id.spinner_age_group);

        String[] types = {AppConstants.CONTENT_TYPE_QUIZ, AppConstants.CONTENT_TYPE_GAME, AppConstants.CONTENT_TYPE_COUNTING, AppConstants.CONTENT_TYPE_COLOR};
        String[] ages = {AppConstants.AGE_GROUP_3_5, AppConstants.AGE_GROUP_6_8, AppConstants.AGE_GROUP_9_12};
        
        spinnerType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types));
        spinnerAge.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ages));

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String title = tilTitle.getEditText().getText().toString().trim();
                    String desc = tilDesc.getEditText().getText().toString().trim();
                    String type = spinnerType.getSelectedItem().toString();
                    String age = spinnerAge.getSelectedItem().toString();

                    if (title.isEmpty()) {
                        showToast("Vui lòng nhập tên bài học");
                        return;
                    }
                    addNewContent(title, desc, type, age);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void addNewContent(String title, String desc, String type, String age) {
        progressBar.setVisibility(View.VISIBLE);
        String id = UUID.randomUUID().toString();
        ContentCatalog newContent = new ContentCatalog();
        newContent.setContentId(id);
        newContent.setTitle(title);
        newContent.setDescription(desc);
        newContent.setContentType(type);
        newContent.setAgeGroup(age);
        newContent.setStatus(AppConstants.STATUS_ACTIVE);

        contentRepository.createContent(id, newContent)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Đã thêm bài học mới!");
                    loadContent();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Lỗi: " + e.getMessage());
                });
    }

    private void confirmDeleteContent(ContentCatalog content) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa nội dung")
                .setMessage("Bạn có chắc chắn muốn xóa bài học: " + content.getTitle() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteContent(content))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteContent(ContentCatalog content) {
        progressBar.setVisibility(View.VISIBLE);
        contentRepository.softDeleteContent(content.getContentId())
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Đã gỡ bài học");
                    loadContent();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Lỗi xóa: " + e.getMessage());
                });
    }

    private class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ContentViewHolder> {
        private final List<ContentCatalog> list;
        ContentAdapter(List<ContentCatalog> list) { this.list = list; }

        @NonNull
        @Override
        public ContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content_card, parent, false);
            return new ContentViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ContentViewHolder holder, int position) {
            ContentCatalog content = list.get(position);
            holder.tvTitle.setText(content.getTitle());
            holder.tvAge.setText("Lứa tuổi: " + content.getAgeGroup());
            holder.tvTypeTag.setText(content.getContentType() != null ? content.getContentType().toUpperCase() : "CONTENT");
            
            // Map màu cho tag
            if (AppConstants.CONTENT_TYPE_QUIZ.equals(content.getContentType())) {
                holder.tvTypeTag.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.accent_blue)));
            } else if (AppConstants.CONTENT_TYPE_GAME.equals(content.getContentType())) {
                holder.tvTypeTag.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.secondary_yellow)));
            }

            holder.btnDelete.setOnClickListener(v -> confirmDeleteContent(content));
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ContentViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvAge, tvTypeTag;
            ImageView ivThumbnail;
            ImageButton btnDelete;

            ContentViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_content_title);
                tvAge = itemView.findViewById(R.id.tv_content_age);
                tvTypeTag = itemView.findViewById(R.id.tv_content_type_tag);
                ivThumbnail = itemView.findViewById(R.id.iv_content_thumbnail);
                btnDelete = itemView.findViewById(R.id.btn_delete_content);
            }
        }
    }
}

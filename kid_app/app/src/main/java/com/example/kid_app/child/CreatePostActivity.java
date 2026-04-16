package com.example.kid_app.child;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreatePostActivity extends BaseActivity {

    private EditText etContent;
    private ImageView ivPreview;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String selectedMood = "😀";
    private String childName = "Bé";
    private Uri imageUri;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    ivPreview.setImageURI(uri);
                    ivPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    ivPreview.setPadding(0, 0, 0, 0);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        etContent = findViewById(R.id.et_post_content);
        ivPreview = findViewById(R.id.iv_post_preview);

        loadChildName();

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        findViewById(R.id.btn_submit_post).setOnClickListener(v -> {
            if (imageUri != null) {
                uploadImageAndPost();
            } else {
                submitPost(null);
            }
        });

        findViewById(R.id.btn_change_image).setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        setupMoodSelection();
    }

    private void loadChildName() {
        SharedPreferences prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        String selectedChildId = prefs.getString(AppConstants.PREF_SELECTED_CHILD_ID, null);

        if (selectedChildId != null) {
            db.collection(AppConstants.COL_CHILD_PROFILES)
                    .document(selectedChildId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("displayName");
                            if (name == null) name = doc.getString("name");
                            if (name != null && !name.isEmpty()) {
                                childName = name;
                            }
                        }
                    });
        }
    }

    private void uploadImageAndPost() {
        showProgressDialog("Đang đăng bài...");
        String fileName = "posts/" + UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child(fileName);

        ref.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> {
                    submitPost(uri.toString());
                })
                .addOnFailureListener(e -> {
                    hideProgressDialog();
                    showToast("Lỗi tải ảnh: " + e.getMessage());
                });
    }

    private void submitPost(String imageUrl) {
        if (imageUrl == null) showProgressDialog("Đang đăng bài...");

        String content = etContent.getText().toString().trim();
        if (TextUtils.isEmpty(content) && imageUrl == null) {
            hideProgressDialog();
            showToast("Bé hãy viết gì đó hoặc chọn ảnh nhé!");
            return;
        }

        Map<String, Object> post = new HashMap<>();
        post.put("authorName", childName);
        post.put("content", content + " " + selectedMood);
        post.put("likes", 0);
        post.put("createdAt", FieldValue.serverTimestamp());
        post.put("type", "student");
        if (imageUrl != null) {
            post.put("imageUrl", imageUrl);
        }

        db.collection("posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    hideProgressDialog();
                    showToast("Đã đăng bài thành công! 🎉");
                    finish();
                })
                .addOnFailureListener(e -> {
                    hideProgressDialog();
                    showToast("Lỗi khi đăng bài: " + e.getMessage());
                });
    }

    private void setupMoodSelection() {
        View moodHappy = findViewById(R.id.btn_mood_happy);
        View moodCool = findViewById(R.id.btn_mood_cool);
        View moodLove = findViewById(R.id.btn_mood_love);

        moodHappy.setOnClickListener(v -> {
            selectedMood = "😀";
            selectMood(moodHappy, moodCool, moodLove);
        });
        moodCool.setOnClickListener(v -> {
            selectedMood = "😎";
            selectMood(moodCool, moodHappy, moodLove);
        });
        moodLove.setOnClickListener(v -> {
            selectedMood = "😍";
            selectMood(moodLove, moodHappy, moodCool);
        });
    }

    private void selectMood(View selected, View... others) {
        selected.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.secondary_yellow)));
        for (View v : others) {
            v.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.white)));
        }
    }
}

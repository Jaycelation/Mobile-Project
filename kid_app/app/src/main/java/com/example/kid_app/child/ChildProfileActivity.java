package com.example.kid_app.child;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.kid_app.R;
import com.example.kid_app.auth.AuthService;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.mapper.DocumentMapper;
import com.example.kid_app.data.model.ChildProfile;
import com.example.kid_app.data.repository.ChildProfileRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class ChildProfileActivity extends BaseActivity {

    private TextView tvName, tvClassInfo;
    private ChildProfileRepository childProfileRepository;
    private AuthService authService;
    private String childId;
    private ListenerRegistration childListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_profile);

        childProfileRepository = new ChildProfileRepository();
        authService = new AuthService();
        
        SharedPreferences prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        childId = prefs.getString(AppConstants.PREF_SELECTED_CHILD_ID, null);

        bindViews();
        bindBottomNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        listenToChildData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (childListener != null) {
            childListener.remove();
        }
    }

    private void bindViews() {
        tvName = findViewById(R.id.tv_child_name);
        tvClassInfo = findViewById(R.id.tv_class_info);

        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        findViewById(R.id.btn_settings_profile).setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.kid_app.parent.ChildSettingsActivity.class);
            intent.putExtra(AppConstants.KEY_CHILD_ID, childId);
            startActivity(intent);
        });

        findViewById(R.id.btn_edit_profile).setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.kid_app.parent.EditChildActivity.class);
            intent.putExtra(AppConstants.KEY_CHILD_ID, childId);
            startActivity(intent);
        });

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            authService.signOut();
            getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE)
                    .edit()
                    .remove(AppConstants.PREF_SELECTED_CHILD_ID)
                    .apply();
            navigateToClearStack(com.example.kid_app.WelcomeActivity.class);
        });
        
        View viewAll = findViewById(R.id.tv_view_all_badges);
        if (viewAll != null) {
            viewAll.setOnClickListener(v -> {
                startActivity(new Intent(this, BadgeCollectionActivity.class));
            });
        }
    }

    private void listenToChildData() {
        if (childId == null) return;

        childListener = FirebaseFirestore.getInstance()
                .collection(AppConstants.COL_CHILD_PROFILES)
                .document(childId)
                .addSnapshotListener((doc, e) -> {
                    if (doc != null && doc.exists()) {
                        ChildProfile profile = DocumentMapper.toChildProfile(doc);
                        if (profile != null) {
                            if (tvName != null) tvName.setText(profile.getDisplayName());
                            
                            String classId = profile.getPrimaryClassId();
                            String className = profile.getClassName();
                            
                            if (classId == null || classId.isEmpty()) {
                                tvClassInfo.setText("Chưa tham gia lớp học");
                            } else {
                                tvClassInfo.setText(className != null ? className : "Đã vào lớp");
                            }
                        }
                    }
                });
    }

    private void bindBottomNavigation() {
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(this, ChildHomeActivity.class));
            finish();
        });
        findViewById(R.id.nav_community).setOnClickListener(v -> {
            startActivity(new Intent(this, CommunityFeedActivity.class));
            finish();
        });
        findViewById(R.id.nav_progress).setOnClickListener(v -> {
            startActivity(new Intent(this, ChildProgressActivity.class));
            finish();
        });
    }
}

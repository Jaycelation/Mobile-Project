package com.example.kid_app.parent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kid_app.R;
import com.example.kid_app.auth.AuthService;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.mapper.DocumentMapper;
import com.example.kid_app.data.model.Account;
import com.example.kid_app.data.model.ChildProfile;
import com.example.kid_app.data.model.ParentChildLink;
import com.example.kid_app.data.repository.ChildProfileRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * ParentHomeActivity — Trang chủ của Phụ huynh (Bước 4).
 *
 * Chức năng:
 * 1. Hiển thị tên phụ huynh.
 * 2. Tải danh sách hồ sơ bé qua parent_child_links.
 * 3. Thêm bé mới → AddChildActivity.
 * 4. Chọn bé → lưu selectedChildId vào SharedPreferences (cho Bước 5 dùng).
 * 5. Mở Settings → ChildSettingsActivity.
 * 6. Đăng xuất.
 *
 * Flow child selection:
 * Phụ huynh chọn bé → lưu childId vào prefs → điều hướng tới ChildHomeActivity (Bước 5).
 * Hiện tại chỉ toast xác nhận, placeholder điều hướng sẽ thêm ở Bước 5.
 */
public class ParentHomeActivity extends BaseActivity {

    private AuthService           authService;
    private ChildProfileService   childProfileService;
    private ChildProfileRepository childProfileRepository;

    private TextView     tvParentName;
    private ProgressBar  progressBar;
    private RecyclerView rvChildren;
    private LinearLayout layoutEmpty;

    private ChildAdapter    childAdapter;
    private List<ChildProfile> childList = new ArrayList<>();

    // Launcher để nhận kết quả từ Add/Edit → reload danh sách
    private final ActivityResultLauncher<Intent> childFormLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadChildren(); // reload sau khi tạo/sửa/xóa bé
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_home);

        authService            = new AuthService();
        childProfileService    = new ChildProfileService();
        childProfileRepository  = new ChildProfileRepository();

        bindViews();
        loadParentName();
        loadChildren();
    }

    private void bindViews() {
        tvParentName = findViewById(R.id.tv_parent_name);
        progressBar  = findViewById(R.id.progress_bar);
        rvChildren   = findViewById(R.id.rv_children);
        layoutEmpty  = findViewById(R.id.layout_empty);

        // RecyclerView setup
        childAdapter = new ChildAdapter(childList, new ChildAdapter.ChildClickListener() {
            @Override
            public void onChildClick(ChildProfile child) {
                selectChild(child);
            }

            @Override
            public void onEditClick(ChildProfile child) {
                Intent intent = new Intent(ParentHomeActivity.this, EditChildActivity.class);
                intent.putExtra(AppConstants.KEY_CHILD_ID, child.getChildId());
                childFormLauncher.launch(intent);
            }

            @Override
            public void onSettingsClick(ChildProfile child) {
                openChildSettings(child);
            }
        });
        rvChildren.setLayoutManager(new LinearLayoutManager(this));
        rvChildren.setAdapter(childAdapter);

        // Nút thêm bé
        MaterialButton btnAddChild = findViewById(R.id.btn_add_child);
        btnAddChild.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddChildActivity.class);
            childFormLauncher.launch(intent);
        });

        // Nút đăng xuất
        android.widget.ImageButton btnSignOut = findViewById(R.id.btn_sign_out);
        btnSignOut.setOnClickListener(v -> signOut());
    }

    // ==================== LOAD DATA ====================

    private void loadParentName() {
        authService.getCurrentUserAccount()
                .addOnSuccessListener(doc -> {
                    Account account = DocumentMapper.toAccount(doc);
                    if (account != null && account.getFullName() != null) {
                        tvParentName.setText(account.getFullName());
                    }
                })
                .addOnFailureListener(e -> { /* giữ text mặc định */ });
    }

    /**
     * Load danh sách bé:
     * 1. Lấy tất cả parent_child_links của parent hiện tại.
     * 2. Với mỗi link → load child_profile tương ứng.
     * 3. Hiển thị vào RecyclerView.
     *
     * Cách hiệu quả hơn khi scale: dùng whereIn query.
     * Hiện tại dùng vòng lặp vì số bé ít (thường < 5).
     */
    private void loadChildren() {
        String parentId = authService.getCurrentUser() != null
                ? authService.getCurrentUser().getUid() : null;
        if (parentId == null) return;

        showLoading(progressBar);
        childList.clear();
        childAdapter.notifyDataSetChanged();

        childProfileService.getChildrenLinksOfParent(parentId)
                .addOnSuccessListener(querySnapshot -> {
                    List<ParentChildLink> links = DocumentMapper.listParentChildLinks(querySnapshot);

                    if (links.isEmpty()) {
                        hideLoading(progressBar);
                        showEmptyState(true);
                        return;
                    }

                    // Dùng counter để biết khi nào load xong tất cả
                    final int[] remaining = {links.size()};

                    for (ParentChildLink link : links) {
                        childProfileRepository.getChildProfile(link.getChildId())
                                .addOnSuccessListener(doc -> {
                                    ChildProfile profile = DocumentMapper.toChildProfile(doc);
                                    if (profile != null && !profile.isDeleted()) {
                                        childList.add(profile);
                                    }
                                    remaining[0]--;
                                    if (remaining[0] == 0) {
                                        hideLoading(progressBar);
                                        showEmptyState(childList.isEmpty());
                                        childAdapter.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    remaining[0]--;
                                    if (remaining[0] == 0) {
                                        hideLoading(progressBar);
                                        showEmptyState(childList.isEmpty());
                                        childAdapter.notifyDataSetChanged();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    hideLoading(progressBar);
                    showToast("Không tải được danh sách bé: " + e.getMessage());
                });
    }

    // ==================== ACTIONS ====================

    /**
     * Phụ huynh chọn bé để "vào chế độ học".
     * Lưu childId vào SharedPreferences — ChildHomeActivity (Bước 5) sẽ đọc.
     */
    private void selectChild(ChildProfile child) {
        getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE)
                .edit()
                .putString(AppConstants.PREF_SELECTED_CHILD_ID, child.getChildId())
                .apply();

        // Bước 5: điều hướng thật sang ChildHomeActivity
        navigateTo(com.example.kid_app.child.ChildHomeActivity.class);
    }

    private void openChildSettings(ChildProfile child) {
        Intent intent = new Intent(this, ChildSettingsActivity.class);
        intent.putExtra(AppConstants.KEY_CHILD_ID, child.getChildId());
        childFormLauncher.launch(intent);
    }

    private void signOut() {
        authService.signOut();
        getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE)
                .edit()
                .remove(AppConstants.PREF_SELECTED_CHILD_ID)
                .apply();
        navigateToClearStack(com.example.kid_app.WelcomeActivity.class);
    }

    // ==================== UI HELPERS ====================

    private void showEmptyState(boolean show) {
        layoutEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        rvChildren.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}

package com.example.kid_app.parent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
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

import java.util.ArrayList;
import java.util.List;

public class ParentHomeActivity extends BaseActivity {

    private AuthService authService;
    private ChildProfileService childProfileService;
    private ChildProfileRepository childProfileRepository;

    private TextView tvParentName;
    private ProgressBar progressBar;
    private RecyclerView rvChildren;
    private LinearLayout layoutEmpty;

    private ChildAdapter childAdapter;
    private List<ChildProfile> childList = new ArrayList<>();

    private final ActivityResultLauncher<Intent> childFormLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadChildren();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_home);

        authService = new AuthService();
        childProfileService = new ChildProfileService();
        childProfileRepository = new ChildProfileRepository();

        bindViews();
        loadParentName();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // LUÔN LÀM MỚI DANH SÁCH KHI QUAY LẠI MÀN HÌNH NÀY
        loadChildren();
    }

    private void bindViews() {
        tvParentName = findViewById(R.id.tv_parent_name);
        progressBar = findViewById(R.id.progress_bar);
        rvChildren = findViewById(R.id.rv_children);
        layoutEmpty = findViewById(R.id.layout_empty);

        childAdapter = new ChildAdapter(childList, new ChildAdapter.ChildClickListener() {
            @Override
            public void onChildClick(ChildProfile child) {
                if (child != null && child.getChildId() != null) {
                    selectChild(child);
                }
            }

            @Override
            public void onEditClick(ChildProfile child) {
                Intent intent = new Intent(ParentHomeActivity.this, EditChildActivity.class);
                intent.putExtra(AppConstants.KEY_CHILD_ID, child.getChildId());
                childFormLauncher.launch(intent);
            }

            @Override
            public void onDeleteClick(ChildProfile child) {
                confirmDeleteChild(child);
            }
        });
        rvChildren.setLayoutManager(new LinearLayoutManager(this));
        rvChildren.setAdapter(childAdapter);

        findViewById(R.id.btn_add_child).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddChildActivity.class);
            childFormLauncher.launch(intent);
        });

        findViewById(R.id.btn_join_class_portal).setOnClickListener(v -> showChildSelectionDialog());

        findViewById(R.id.btn_sign_out).setOnClickListener(v -> signOut());
    }

    private void confirmDeleteChild(ChildProfile child) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa hồ sơ bé")
                .setMessage("Bạn có chắc chắn muốn xóa hồ sơ của bé " + child.getDisplayName() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteChild(child);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteChild(ChildProfile child) {
        showLoading(progressBar);
        childProfileRepository.hardDeleteChildProfile(child.getChildId())
                .addOnSuccessListener(v -> {
                    hideLoading(progressBar);
                    Toast.makeText(this, "Đã xóa sạch hồ sơ và dữ liệu liên quan thành công", Toast.LENGTH_SHORT).show();
                    loadChildren();
                })
                .addOnFailureListener(e -> {
                    hideLoading(progressBar);
                    Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showChildSelectionDialog() {
        if (childList.isEmpty()) {
            Toast.makeText(this, "Vui lòng tạo hồ sơ bé trước!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (childList.size() == 1) {
            openJoinClass(childList.get(0));
        } else {
            String[] names = new String[childList.size()];
            for (int i = 0; i < childList.size(); i++) {
                names[i] = childList.get(i).getDisplayName();
            }

            new AlertDialog.Builder(this)
                    .setTitle("Chọn bé tham gia lớp học")
                    .setItems(names, (dialog, which) -> openJoinClass(childList.get(which)))
                    .setNegativeButton("Hủy", null)
                    .show();
        }
    }

    private void openJoinClass(ChildProfile child) {
        Intent intent = new Intent(this, JoinClassActivity.class);
        intent.putExtra(AppConstants.KEY_CHILD_ID, child.getChildId());
        intent.putExtra("child_name", child.getDisplayName());
        startActivity(intent);
    }

    private void loadParentName() {
        authService.getCurrentUserAccount()
                .addOnSuccessListener(doc -> {
                    Account account = DocumentMapper.toAccount(doc);
                    if (account != null && account.getFullName() != null) {
                        tvParentName.setText(account.getFullName());
                    }
                });
    }

    private void loadChildren() {
        String parentId = authService.getCurrentUser() != null ? authService.getCurrentUser().getUid() : null;
        if (parentId == null) return;

        showLoading(progressBar);
        childProfileService.getChildrenLinksOfParent(parentId)
                .addOnSuccessListener(querySnapshot -> {
                    List<ParentChildLink> links = DocumentMapper.listParentChildLinks(querySnapshot);
                    if (links.isEmpty()) {
                        childList.clear();
                        childAdapter.notifyDataSetChanged();
                        hideLoading(progressBar);
                        showEmptyState(true);
                        return;
                    }

                    childList.clear();
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
                });
    }

    private void selectChild(ChildProfile child) {
        getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE)
                .edit()
                .putString(AppConstants.PREF_SELECTED_CHILD_ID, child.getChildId())
                .apply();
        
        Intent intent = new Intent(this, com.example.kid_app.child.ChildHomeActivity.class);
        intent.putExtra(AppConstants.KEY_CHILD_ID, child.getChildId());
        startActivity(intent);
    }

    private void signOut() {
        authService.signOut();
        getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE).edit().remove(AppConstants.PREF_SELECTED_CHILD_ID).apply();
        navigateToClearStack(com.example.kid_app.WelcomeActivity.class);
    }

    private void showEmptyState(boolean show) {
        if (layoutEmpty != null) layoutEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        if (rvChildren != null) rvChildren.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}

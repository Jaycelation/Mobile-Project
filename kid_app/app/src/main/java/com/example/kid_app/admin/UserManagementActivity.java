package com.example.kid_app.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kid_app.R;
import com.example.kid_app.auth.AuthService;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.mapper.DocumentMapper;
import com.example.kid_app.data.model.Account;
import com.example.kid_app.data.repository.AccountRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserManagementActivity extends BaseActivity {

    private RecyclerView rvUsers;
    private ProgressBar progressBar;
    private UserAdapter adapter;
    private AccountRepository accountRepository;
    private AuthService authService;
    private List<Account> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        accountRepository = new AccountRepository();
        authService = new AuthService();
        
        rvUsers = findViewById(R.id.rv_users);
        progressBar = findViewById(R.id.progress_bar);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_teacher);
        
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        fabAdd.setOnClickListener(v -> showAddTeacherDialog());

        setupRecyclerView();
        loadUsers();
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter(userList);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        accountRepository.getAllAccounts()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    userList.clear();
                    userList.addAll(DocumentMapper.listAccounts(querySnapshot));
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Lỗi tải danh sách: " + e.getMessage());
                });
    }

    private void showAddTeacherDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_teacher, null);
        TextInputLayout tilName = dialogView.findViewById(R.id.til_teacher_name);
        TextInputLayout tilEmail = dialogView.findViewById(R.id.til_teacher_email);
        TextInputLayout tilPass = dialogView.findViewById(R.id.til_teacher_password);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Tạo tài khoản", (dialog, which) -> {
                    String name = tilName.getEditText().getText().toString().trim();
                    String email = tilEmail.getEditText().getText().toString().trim();
                    String pass = tilPass.getEditText().getText().toString().trim();
                    
                    if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                        showToast("Vui lòng nhập đầy đủ thông tin");
                        return;
                    }
                    createTeacherAccount(email, pass, name);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void createTeacherAccount(String email, String pass, String name) {
        progressBar.setVisibility(View.VISIBLE);
        authService.signUp(email, pass, name, AppConstants.ROLE_TEACHER)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Đã tạo tài khoản Giáo viên thành công!");
                    loadUsers();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Lỗi: " + e.getMessage());
                });
    }

    private void confirmDeleteUser(Account account) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa người dùng")
                .setMessage("Bạn có chắc chắn muốn xóa " + account.getFullName() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteUser(account))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteUser(Account account) {
        progressBar.setVisibility(View.VISIBLE);
        // Sửa từ softDeleteAccount sang deleteAccount để khớp với Repository mới
        accountRepository.deleteAccount(account.getAccountId())
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Đã xóa người dùng");
                    loadUsers(); // Refresh danh sách
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Lỗi xóa: " + e.getMessage());
                });
    }

    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private final List<Account> list;
        UserAdapter(List<Account> list) { this.list = list; }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_card, parent, false);
            return new UserViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            Account account = list.get(position);
            holder.tvName.setText(account.getFullName());
            holder.tvEmail.setText(account.getEmail());
            holder.tvRole.setText(account.getRole() != null ? account.getRole().toUpperCase() : "USER");
            holder.btnDelete.setOnClickListener(v -> confirmDeleteUser(account));
        }

        @Override
        public int getItemCount() { return list.size(); }

        class UserViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvEmail, tvRole;
            ImageButton btnDelete;
            UserViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_user_name);
                tvEmail = itemView.findViewById(R.id.tv_user_email);
                tvRole = itemView.findViewById(R.id.tv_user_role);
                btnDelete = itemView.findViewById(R.id.btn_delete_user);
            }
        }
    }
}

package com.example.kid_app.parent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kid_app.R;
import com.example.kid_app.data.model.ChildProfile;

import java.util.List;

/**
 * ChildAdapter — hiển thị danh sách hồ sơ bé trong RecyclerView.
 */
public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ChildViewHolder> {

    public interface ChildClickListener {
        void onChildClick(ChildProfile child);
        void onEditClick(ChildProfile child);
        void onDeleteClick(ChildProfile child); // Thêm sự kiện xóa
    }

    private final List<ChildProfile> children;
    private final ChildClickListener listener;

    public ChildAdapter(List<ChildProfile> children, ChildClickListener listener) {
        this.children = children;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_child_card, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        holder.bind(children.get(position), listener);
    }

    @Override
    public int getItemCount() { return children.size(); }

    // ==================== VIEW HOLDER ====================

    static class ChildViewHolder extends RecyclerView.ViewHolder {

        private final TextView    tvAvatar;
        private final TextView    tvName;
        private final TextView    tvAgeGroup;
        private final TextView    tvBirth;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete; // Nút xóa mới

        ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar    = itemView.findViewById(R.id.tv_avatar);
            tvName      = itemView.findViewById(R.id.tv_child_name);
            tvAgeGroup  = itemView.findViewById(R.id.tv_child_age_group);
            tvBirth     = itemView.findViewById(R.id.tv_child_birth);
            btnEdit     = itemView.findViewById(R.id.btn_child_edit);
            btnDelete   = itemView.findViewById(R.id.btn_child_delete);
        }

        void bind(ChildProfile child, ChildClickListener listener) {
            tvName.setText(child.getDisplayName());

            String ag = child.getAgeGroup();
            tvAgeGroup.setText("Nhóm tuổi: " + (ag != null ? ag : "--") + " tuổi");

            String birth = child.getBirthDate();
            tvBirth.setText("Ngày sinh: " + (birth != null && !birth.isEmpty() ? birth : "--"));

            // Avatar theo giới tính
            String gender = child.getGender();
            tvAvatar.setText("female".equals(gender) ? "👧" : "👦");

            // Click cả card → chọn bé vào chế độ học
            itemView.setOnClickListener(v -> listener.onChildClick(child));

            // Nút Edit
            if (btnEdit != null) {
                btnEdit.setOnClickListener(v -> listener.onEditClick(child));
            }

            // Nút Delete
            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> listener.onDeleteClick(child));
            }
        }
    }
}

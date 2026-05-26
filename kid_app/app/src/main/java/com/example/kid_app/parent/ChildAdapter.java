package com.example.kid_app.parent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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
        void onDeleteClick(ChildProfile child);
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

    static class ChildViewHolder extends RecyclerView.ViewHolder {

        private final ImageView   ivAvatar;
        private final TextView    tvName;
        private final TextView    tvAgeGroup;
        private final TextView    tvBirth;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;

        ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar    = itemView.findViewById(R.id.iv_avatar);
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

            // Thay đổi avatar dựa trên giới tính từ ảnh em gửi
            String gender = child.getGender();
            if ("female".equals(gender)) {
                ivAvatar.setImageResource(R.drawable.hoc_sinh_nu);
            } else {
                ivAvatar.setImageResource(R.drawable.hoc_sinh_nam);
            }

            itemView.setOnClickListener(v -> listener.onChildClick(child));

            if (btnEdit != null) {
                btnEdit.setOnClickListener(v -> listener.onEditClick(child));
            }

            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> listener.onDeleteClick(child));
            }
        }
    }
}

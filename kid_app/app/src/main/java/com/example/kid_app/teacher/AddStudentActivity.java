package com.example.kid_app.teacher;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kid_app.R;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.mapper.DocumentMapper;
import com.example.kid_app.data.model.ChildProfile;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddStudentActivity extends BaseActivity {

    private TextView tvJoinCode;
    private EditText etSearchQuery;
    private RecyclerView rvResults;
    private ImageView ivQrCode;
    private SearchAdapter adapter;
    private List<ChildProfile> searchResults = new ArrayList<>();
    
    private FirebaseFirestore db;
    private String classId, joinCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        db = FirebaseFirestore.getInstance();
        classId = getIntent().getStringExtra("class_id");
        joinCode = getIntent().getStringExtra("join_code");

        bindViews();
        setupData();
        generateQRCode(joinCode);
    }

    private void bindViews() {
        tvJoinCode = findViewById(R.id.tv_join_code);
        etSearchQuery = findViewById(R.id.et_search_query);
        rvResults = findViewById(R.id.rv_search_results);
        ivQrCode = findViewById(R.id.iv_qr_code);
        
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchAdapter(searchResults);
        rvResults.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // SỬA LỖI SAO CHÉP: Lấy trực tiếp text từ TextView
        findViewById(R.id.btn_copy_code).setOnClickListener(v -> {
            String codeToCopy = tvJoinCode.getText().toString();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Join Code", codeToCopy);
            clipboard.setPrimaryClip(clip);
            
            // Thông báo thêm để người dùng biết chắc chắn đã copy
            Toast.makeText(this, "Đã sao chép mã: " + codeToCopy, Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_share_code).setOnClickListener(v -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Tham gia lớp học của thầy/cô với mã: " + joinCode);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Chia sẻ mã qua:"));
        });

        findViewById(R.id.btn_do_search).setOnClickListener(v -> searchStudents());
    }

    private void setupData() {
        if (joinCode != null) tvJoinCode.setText(joinCode);
    }

    // TÍNH NĂNG MỚI: TẠO MÃ QR
    private void generateQRCode(String text) {
        if (text == null || text.isEmpty()) return;
        
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            ivQrCode.setImageBitmap(bmp);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void searchStudents() {
        String query = etSearchQuery.getText().toString().trim();
        if (query.isEmpty()) return;

        db.collection("child_profiles")
                .whereGreaterThanOrEqualTo("displayName", query)
                .whereLessThanOrEqualTo("displayName", query + "\uf8ff")
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    searchResults.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ChildProfile profile = DocumentMapper.toChildProfile(doc);
                        if (profile != null) searchResults.add(profile);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void addStudentToClass(ChildProfile child) {
        Map<String, Object> member = new HashMap<>();
        member.put("classId", classId);
        member.put("childId", child.getChildId());
        member.put("studentName", child.getDisplayName());
        member.put("joinedAt", com.google.firebase.Timestamp.now());

        db.collection("class_members").add(member)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Đã thêm " + child.getDisplayName() + " vào lớp! 🎉", Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
        private List<ChildProfile> list;
        public SearchAdapter(List<ChildProfile> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_in_class, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChildProfile child = list.get(position);
            holder.tvName.setText(child.getDisplayName());
            holder.tvInfo.setText("Mã: " + (child.getChildId() != null ? child.getChildId().substring(0, 8) : "N/A"));
            
            if (holder.btnAction != null) {
                holder.btnAction.setImageResource(android.R.drawable.ic_input_add);
                holder.btnAction.setOnClickListener(v -> addStudentToClass(child));
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvInfo;
            ImageButton btnAction;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_student_name);
                tvInfo = itemView.findViewById(R.id.tv_parent_info);
                btnAction = itemView.findViewById(R.id.btn_student_action);
            }
        }
    }
}

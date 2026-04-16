package com.example.kid_app.child;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.model.FeedbackMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ParentFeedbackActivity extends BaseActivity {
    private RecyclerView rvChat;
    private EditText etInput;
    private ChatAdapter adapter;
    private List<FeedbackMessage> messageList = new ArrayList<>();
    private FirebaseFirestore db;
    private String feedbackId, selectedChildId;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_chat);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid(); // UID của phụ huynh đang đăng nhập
        
        SharedPreferences prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        selectedChildId = prefs.getString(AppConstants.PREF_SELECTED_CHILD_ID, null);
        
        // Nhận feedbackId từ Intent nếu có (khi mở từ danh sách)
        feedbackId = getIntent().getStringExtra("feedback_id");

        initViews();
        
        if (feedbackId != null) {
            loadMessages();
        } else {
            findOrCreateFeedbackSession();
        }
    }

    private void initViews() {
        TextView tvTitle = findViewById(R.id.tv_chat_title);
        if (tvTitle != null) tvTitle.setText("Trao đổi với cô giáo");
        
        rvChat = findViewById(R.id.rv_chat_messages);
        etInput = findViewById(R.id.et_message_input);
        
        if (rvChat != null) {
            rvChat.setLayoutManager(new LinearLayoutManager(this));
            adapter = new ChatAdapter(messageList);
            rvChat.setAdapter(adapter);
        }

        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        
        View btnSend = findViewById(R.id.btn_send);
        if (btnSend != null) btnSend.setOnClickListener(v -> sendMessage());
    }

    private void findOrCreateFeedbackSession() {
        if (selectedChildId == null) return;
        
        db.collection("feedback_notes")
                .whereEqualTo("childId", selectedChildId)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        feedbackId = snap.getDocuments().get(0).getId();
                        loadMessages();
                    } else {
                        createNewSession();
                    }
                });
    }

    private void createNewSession() {
        Map<String, Object> session = new HashMap<>();
        session.put("childId", selectedChildId);
        session.put("noteText", "Bắt đầu cuộc hội thoại");
        session.put("createdAt", FieldValue.serverTimestamp());
        
        db.collection("feedback_notes").add(session).addOnSuccessListener(ref -> {
            feedbackId = ref.getId();
            loadMessages();
        });
    }

    private void loadMessages() {
        if (feedbackId == null) return;
        
        db.collection("feedback_notes")
                .document(feedbackId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((v, e) -> {
                    if (v != null) {
                        messageList.clear();
                        messageList.addAll(v.toObjects(FeedbackMessage.class));
                        adapter.notifyDataSetChanged();
                        if (!messageList.isEmpty()) {
                            rvChat.scrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (TextUtils.isEmpty(text) || feedbackId == null) return;

        // Lưu tin nhắn với UID của phụ huynh và role "parent"
        FeedbackMessage msg = new FeedbackMessage(currentUserId, "parent", text);
        db.collection("feedback_notes")
                .document(feedbackId)
                .collection("messages")
                .add(msg)
                .addOnSuccessListener(d -> {
                    etInput.setText("");
                    db.collection("feedback_notes").document(feedbackId).update("noteText", text);
                });
    }

    // Logic: Nếu senderId khớp với UID của tôi -> Đó là tin nhắn của tôi (Bên phải)
    private boolean isMe(FeedbackMessage message) {
        return currentUserId != null && currentUserId.equals(message.getSenderId());
    }

    private class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<FeedbackMessage> list;
        private static final int TYPE_ME = 1;     // Phải
        private static final int TYPE_OTHER = 2;  // Trái
        private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        public ChatAdapter(List<FeedbackMessage> list) { this.list = list; }

        @Override
        public int getItemViewType(int position) {
            return isMe(list.get(position)) ? TYPE_ME : TYPE_OTHER;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // TYPE_ME dùng layout bên phải (item_chat_teacher)
            // TYPE_OTHER dùng layout bên trái (item_chat_parent)
            int layout = (viewType == TYPE_ME) ? R.layout.item_chat_teacher : R.layout.item_chat_parent;
            View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
            return new MessageViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            FeedbackMessage msg = list.get(position);
            MessageViewHolder vh = (MessageViewHolder) holder;
            vh.tvText.setText(msg.getMessageText());
            
            if (msg.getCreatedAt() != null) {
                vh.tvTime.setText(sdf.format(msg.getCreatedAt()));
                vh.tvTime.setVisibility(View.VISIBLE);
            } else {
                vh.tvTime.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() { return list.size(); }

        class MessageViewHolder extends RecyclerView.ViewHolder {
            TextView tvText, tvTime;
            MessageViewHolder(View v) { 
                super(v);
                tvText = v.findViewById(R.id.tv_message_text);
                tvTime = v.findViewById(R.id.tv_time);
            }
        }
    }
}

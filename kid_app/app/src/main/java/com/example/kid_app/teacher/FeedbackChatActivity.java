package com.example.kid_app.teacher;

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
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.model.FeedbackMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FeedbackChatActivity extends BaseActivity {

    protected RecyclerView rvChat;
    protected EditText etInput;
    protected ChatAdapter adapter;
    protected List<FeedbackMessage> messageList = new ArrayList<>();
    protected FirebaseFirestore db;
    protected String feedbackId;
    protected String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_chat);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid(); // UID của giáo viên đang đăng nhập
        feedbackId = getIntent().getStringExtra("feedback_id");

        if (feedbackId == null) {
            finish();
            return;
        }

        rvChat = findViewById(R.id.rv_chat_messages);
        etInput = findViewById(R.id.et_message_input);
        
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(messageList);
        rvChat.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_send).setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private void loadMessages() {
        db.collection("feedback_notes")
                .document(feedbackId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        messageList.clear();
                        messageList.addAll(value.toObjects(FeedbackMessage.class));
                        adapter.notifyDataSetChanged();
                        if (!messageList.isEmpty()) {
                            rvChat.scrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    protected void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        // Lưu tin nhắn với UID của mình và role "teacher"
        FeedbackMessage msg = new FeedbackMessage(currentUserId, "teacher", text);
        
        db.collection("feedback_notes")
                .document(feedbackId)
                .collection("messages")
                .add(msg)
                .addOnSuccessListener(doc -> {
                    etInput.setText("");
                    db.collection("feedback_notes").document(feedbackId).update("noteText", text);
                });
    }

    // Logic: Nếu senderId khớp với UID của tôi -> Đó là tin nhắn của tôi (Bên phải)
    protected boolean isMe(FeedbackMessage message) {
        return currentUserId != null && currentUserId.equals(message.getSenderId());
    }

    protected class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<FeedbackMessage> list;
        private static final int TYPE_ME = 1;     // Tin nhắn của tôi (Phải)
        private static final int TYPE_OTHER = 2;  // Tin nhắn người khác (Trái)
        private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        public ChatAdapter(List<FeedbackMessage> list) { this.list = list; }

        @Override
        public int getItemViewType(int position) {
            return isMe(list.get(position)) ? TYPE_ME : TYPE_OTHER;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // TYPE_ME dùng item_chat_teacher (có gravity=end - Bên phải)
            // TYPE_OTHER dùng item_chat_parent (có gravity=start - Bên trái)
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
            public MessageViewHolder(@NonNull View itemView) {
                super(itemView);
                tvText = itemView.findViewById(R.id.tv_message_text);
                tvTime = itemView.findViewById(R.id.tv_time);
            }
        }
    }
}

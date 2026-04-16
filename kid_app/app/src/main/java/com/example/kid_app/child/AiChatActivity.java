package com.example.kid_app.child;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kid_app.R;
import com.example.kid_app.common.BaseActivity;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class AiChatActivity extends BaseActivity {

    private static final String TAG = "AiChatActivity";
    private RecyclerView rvChat;
    private EditText etInput;
    private ImageButton btnSend;
    private ChatAdapter adapter;
    private List<Message> messageList = new ArrayList<>();
    
    private GenerativeModelFutures model;
    
    private String childName = "bé"; // Tên mặc định


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        loadChildContext();
        initGemini();
        
        rvChat = findViewById(R.id.rv_chat_messages);
        etInput = findViewById(R.id.et_ai_input);
        btnSend = findViewById(R.id.btn_ai_send);

        adapter = new ChatAdapter(messageList);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        findViewById(R.id.btn_ai_back).setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> sendMessage());

        addMessage("Chào bé! Tớ là Robot Thông Thái. Bé muốn hỏi tớ điều gì hôm nay nào?", false);
    }

    private void loadChildContext() {
        android.content.SharedPreferences prefs = getSharedPreferences(com.example.kid_app.common.AppConstants.PREF_NAME, MODE_PRIVATE);
        String childId = prefs.getString(com.example.kid_app.common.AppConstants.PREF_SELECTED_CHILD_ID, null);
        if (childId != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection(com.example.kid_app.common.AppConstants.COL_CHILD_PROFILES)
                .document(childId).get().addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name == null || name.isEmpty()) name = doc.getString("fullName");
                        if (name != null) childName = name;
                    }
                });
        }
    }

    private void initGemini() {
        String apiKey = com.example.kid_app.BuildConfig.GEMINI_API_KEY;
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("\"\"")) {
            apiKey = "dummy_key";
        }
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", apiKey);
        model = GenerativeModelFutures.from(gm);
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (text.isEmpty()) return;

        addMessage(text, true);
        etInput.setText("");

        Content content = new Content.Builder()
                .addText("Bạn là một Robot dạy học vui vẻ dành cho trẻ em từ 3-8 tuổi. Bạn đang nói chuyện với một bé tên là " + childName + ". Hãy trả lời câu hỏi của bé cực kỳ ngắn gọn, vui vẻ, thân thiện bằng tiếng Việt: " + text)
                .build();

        Executor mainExecutor = ContextCompat.getMainExecutor(this);
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String aiResponse = result.getText();
                if (aiResponse != null && !aiResponse.isEmpty()) {
                    addMessage(aiResponse, false);
                } else {
                    addMessage("Robot nhận được dữ liệu trống, bé thử lại nhé!", false);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Lỗi Robot: ", t);
                // Nếu gemini-pro vẫn lỗi 404, có thể do Google AI Studio đang bảo trì khu vực của em
                if (t.getMessage() != null && t.getMessage().contains("404")) {
                    addMessage("Robot đang được nâng cấp hệ thống, bé quay lại sau ít phút nhé!", false);
                } else {
                    addMessage("Lỗi hệ thống: " + t.getMessage(), false);
                }
            }
        }, mainExecutor);
    }

    private void addMessage(String text, boolean isUser) {
        messageList.add(new Message(text, isUser));
        adapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);
    }

    private static class Message {
        String text;
        boolean isUser;
        Message(String text, boolean isUser) { this.text = text; this.isUser = isUser; }
    }

    private class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<Message> list;
        ChatAdapter(List<Message> list) { this.list = list; }

        @Override
        public int getItemViewType(int position) { return list.get(position).isUser ? 1 : 0; }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int res = (viewType == 1) ? R.layout.item_chat_user : R.layout.item_chat_ai;
            View v = LayoutInflater.from(parent.getContext()).inflate(res, parent, false);
            return new RecyclerView.ViewHolder(v) {};
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            TextView tv = holder.itemView.findViewById(R.id.tv_message_text);
            if (tv != null) {
                tv.setText(list.get(position).text);
            }
        }

        @Override
        public int getItemCount() { return list.size(); }
    }
}

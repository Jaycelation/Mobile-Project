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

import com.example.kid_app.BuildConfig;
import com.example.kid_app.R;
import com.example.kid_app.common.AppConstants;
import com.example.kid_app.common.BaseActivity;
import com.example.kid_app.data.model.AiConversation;
import com.example.kid_app.data.model.AiMessage;
import com.example.kid_app.data.repository.AiRepository;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class AiChatActivity extends BaseActivity {

    private static final String TAG = "AiChatActivity";
    private RecyclerView rvChat;
    private EditText etInput;
    private ImageButton btnSend;
    private ChatAdapter adapter;
    private List<AiMessage> messageList = new ArrayList<>();
    
    private GenerativeModelFutures model;
    private String childId;
    private String childName = "bé"; 
    private String conversationId;
    private AiRepository aiRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        aiRepository = new AiRepository();
        initGemini();
        
        rvChat = findViewById(R.id.rv_chat_messages);
        etInput = findViewById(R.id.et_ai_input);
        btnSend = findViewById(R.id.btn_ai_send);

        adapter = new ChatAdapter(messageList);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        findViewById(R.id.btn_ai_back).setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> sendMessage());

        loadChildAndConversation();
    }

    private void initGemini() {
        String apiKey = BuildConfig.GEMINI_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            Log.w(TAG, "GEMINI_API_KEY chưa được cấu hình. AI chat sẽ dùng thông báo fallback.");
            model = null;
            return;
        }

        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", apiKey.trim());
        model = GenerativeModelFutures.from(gm);
    }

    private void loadChildAndConversation() {
        android.content.SharedPreferences prefs = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        childId = prefs.getString(AppConstants.PREF_SELECTED_CHILD_ID, null);
        
        if (childId == null) {
            showToast("Không tìm thấy thông tin bé");
            finish();
            return;
        }

        // Lấy tên bé từ Firestore
        FirebaseFirestore.getInstance()
            .collection(AppConstants.COL_CHILD_PROFILES)
            .document(childId).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String name = doc.getString("name");
                    if (name == null || name.isEmpty()) name = doc.getString("fullName");
                    if (name != null) {
                        childName = name;
                        refreshWelcomeMessage();
                    }
                }
            });

        // ĐỂ XÓA TIN NHẮN CŨ: Chúng ta sẽ tạo một Conversation mới hoàn toàn mỗi khi vào
        String newContextId = "chat_" + System.currentTimeMillis();
        AiConversation newConv = new AiConversation(childId, AppConstants.AI_CONTEXT_FREE, newContextId);
        aiRepository.createConversation(newConv).addOnSuccessListener(docRef -> {
            conversationId = docRef.getId();
            messageList.clear();
            addWelcomeMessage();
        });
    }

    private void refreshWelcomeMessage() {
        if (!messageList.isEmpty() && messageList.get(0).getSenderRole() == AppConstants.AI_ROLE_ASSISTANT) {
            messageList.get(0).setMessageText("Chào " + childName + "! Tớ là Robot Thông Thái. Bé muốn hỏi tớ điều gì nào?");
            adapter.notifyItemChanged(0);
        }
    }

    private void addWelcomeMessage() {
        String welcomeText = "Chào " + childName + "! Tớ là Robot Thông Thái. Bé muốn hỏi tớ điều gì hôm nay nào?";
        AiMessage welcome = new AiMessage(conversationId, AppConstants.AI_ROLE_ASSISTANT, welcomeText);
        messageList.add(welcome);
        adapter.notifyDataSetChanged();
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (text.isEmpty() || conversationId == null) return;

        AiMessage userMsg = new AiMessage(conversationId, AppConstants.AI_ROLE_USER, text);
        messageList.add(userMsg);
        adapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);
        etInput.setText("");

        // Lưu vào Firestore để quản lý nhưng chúng ta đang dùng ID mới nên sẽ không bị lẫn tin nhắn cũ
        aiRepository.addMessage(conversationId, userMsg);
        generateAiResponse(text);
    }

    private void generateAiResponse(String userText) {
        if (model == null) {
            AiMessage fallback = new AiMessage(conversationId, AppConstants.AI_ROLE_ASSISTANT,
                    "Tính năng Robot chưa được cấu hình trên máy này. Phụ huynh hãy thêm GEMINI_API_KEY vào local.properties nhé!");
            runOnUiThread(() -> {
                messageList.add(fallback);
                adapter.notifyItemInserted(messageList.size() - 1);
                rvChat.scrollToPosition(messageList.size() - 1);
            });
            return;
        }

        String safeUserText = userText.length() > 500 ? userText.substring(0, 500) : userText;
        String prompt = "Bạn là Robot dạy học thân thiện, an toàn cho trẻ em. Đang nói chuyện với bé " + childName +
                       ". Trả lời bằng tiếng Việt, cực ngắn trong 1-2 câu, vui vẻ, không đưa nội dung nguy hiểm. Bé hỏi: " + safeUserText;

        Content content = new Content.Builder().addText(prompt).build();
        Executor mainExecutor = ContextCompat.getMainExecutor(this);
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String aiText = result.getText();
                if (aiText != null) {
                    AiMessage aiMsg = new AiMessage(conversationId, AppConstants.AI_ROLE_ASSISTANT, aiText);
                    runOnUiThread(() -> {
                        messageList.add(aiMsg);
                        adapter.notifyItemInserted(messageList.size() - 1);
                        rvChat.scrollToPosition(messageList.size() - 1);
                    });
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Lỗi Robot: " + t.getMessage());
                runOnUiThread(() -> {
                    AiMessage errorMsg = new AiMessage(conversationId, AppConstants.AI_ROLE_ASSISTANT, 
                        "Robot hơi mệt, bé thử hỏi lại sau nhé!");
                    messageList.add(errorMsg);
                    adapter.notifyItemInserted(messageList.size() - 1);
                });
            }
        }, mainExecutor);
    }

    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
        private final List<AiMessage> list;
        ChatAdapter(List<AiMessage> list) { this.list = list; }
        @Override
        public int getItemViewType(int position) { 
            return list.get(position).getSenderRole() == AppConstants.AI_ROLE_USER ? 1 : 0; 
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int res = (viewType == 1) ? R.layout.item_chat_user : R.layout.item_chat_ai;
            View v = LayoutInflater.from(parent.getContext()).inflate(res, parent, false);
            return new ViewHolder(v);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvMessage.setText(list.get(position).getMessageText());
        }
        @Override
        public int getItemCount() { return list.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMessage;
            ViewHolder(View v) { super(v); tvMessage = v.findViewById(R.id.tv_message_text); }
        }
    }
}

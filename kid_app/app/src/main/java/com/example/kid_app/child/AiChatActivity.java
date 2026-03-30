package com.example.kid_app.child;

import android.os.Bundle;

import com.example.kid_app.R;
import com.example.kid_app.common.BaseActivity;

/**
 * AiChatActivity — Placeholder cho module Trợ lý AI (Bước 10).
 *
 * Bước 5: hiển thị placeholder với thông báo.
 * Bước 10: sẽ tích hợp Gemini/OpenAI, lưu ai_conversations và ai_messages,
 *           kiểm tra child_settings.ai_enabled.
 *
 * Lưu ý: AiChatActivity chỉ được mở nếu ai_enabled = true.
 *         ChildHomeActivity đã kiểm tra trước khi điều hướng vào đây.
 */
public class AiChatActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        // Placeholder — Bước 10 sẽ thêm UI chat và tích hợp AI API
        findViewById(R.id.btn_ai_back).setOnClickListener(v -> finish());
    }
}

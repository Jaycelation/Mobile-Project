package com.example.kid_app.ai;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Interface chung cho các dịch vụ AI.
 * Giúp dễ dàng chuyển đổi giữa Gemini, OpenAI hoặc Mock AI.
 */
public interface AiService {
    
    /**
     * Gửi yêu cầu generate nội dung văn bản.
     * @param prompt Câu lệnh hoặc câu hỏi của bé
     * @return Future chứa kết quả trả về từ AI
     */
    ListenableFuture<String> generateResponse(String prompt);
    
    /**
     * Thiết lập ngữ cảnh (System Instruction) cho AI.
     * Ví dụ: "Bạn là một robot vui vẻ..."
     */
    void setSystemInstruction(String instruction);
}

package com.example.kid_app.ai;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Triển khai AiService sử dụng Google Gemini SDK.
 */
public class GeminiService implements AiService {
    private final GenerativeModelFutures model;
    private String systemInstruction = "";

    // Chuc nang: khoi tao model Gemini bang API key duoc cau hinh trong ung dung.
    public GeminiService(String apiKey) {
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", apiKey);
        this.model = GenerativeModelFutures.from(gm);
    }

    @Override
    // Chuc nang: thiet lap huong dan he thong de gioi han cach AI tra loi cho tre em.
    public void setSystemInstruction(String instruction) {
        this.systemInstruction = instruction;
    }

    @Override
    // Chuc nang: goi API Gemini de sinh cau tra loi tu cau hoi cua tre.
    public ListenableFuture<String> generateResponse(String prompt) {
        Content content = new Content.Builder()
                .addText(systemInstruction + "\nCâu hỏi của bé: " + prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        
        // Chuyển đổi từ GenerateContentResponse sang String để Activity dễ dùng
        return Futures.transform(response, result -> {
            if (result != null && result.getText() != null) {
                return result.getText();
            }
            return "Robot không nghe rõ, bé nói lại nhé!";
        }, Runnable::run);
    }
}

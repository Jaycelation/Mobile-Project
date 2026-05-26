# Tài liệu kỹ thuật cá nhân - Nguyễn Hồng Giáp

## 1. Danh sách chức năng được phân công

| Mục | Chức năng | Nhiệm vụ cá nhân |
|---|---|---|
| 2.1.2 | Câu đố và trắc nghiệm | Xây dựng màn hình thi, bộ đếm giờ và logic chấm điểm |
| 2.1.5 | Trợ lý học tập AI thông minh | Tích hợp API trí tuệ nhân tạo và xây dựng giao diện khung chat |

### Chức năng bổ sung không đánh số

| Chức năng/code bổ sung | File/Class liên quan | Ghi chú |
|---|---|---|
| Học chữ cái, cộng đồng và hỗ trợ người dùng | `AlphabetLearningActivity`, `AlphabetMatchGameActivity`, `CommunityFeedActivity`, `CreatePostActivity`, `HelpSupportActivity` | Có code trong dự án nhưng không gán thêm chỉ mục mới |

## 2. Kiến trúc chi tiết module

```text
QuizListActivity
  -> QuizPlayActivity
  -> ActivityAttemptRepository / ContentRepository
  -> content_catalog/{contentId}/questions
  -> child_profiles/{childId}/activity_attempts

AiChatActivity
  -> GeminiService.generateResponse()
  -> AiRepository
  -> ai_conversations / messages

AlphabetLearningActivity / AlphabetMatchGameActivity
  -> ContentRepository / ActivityAttemptRepository

CommunityFeedActivity / CreatePostActivity / HelpSupportActivity
  -> posts / posts/{postId}/comments / Firebase Storage
```

| Thành phần | Vai trò |
|---|---|
| `QuizListActivity` | Hiển thị danh sách bài trắc nghiệm |
| `QuizPlayActivity` | Hiển thị câu hỏi, nhận đáp án và tính điểm |
| `QuizHistoryActivity` | Hiển thị lịch sử làm bài |
| `AiChatActivity` | Giao diện chat với trợ lý AI |
| `GeminiService` | Gọi Gemini API |
| `AiRepository` | Lưu hội thoại và tin nhắn AI |
| `ContentRepository` | Tải quiz và câu hỏi |
| `ActivityAttemptRepository` | Lưu lượt làm quiz và đáp án |
| `AlphabetLearningActivity`, `AlphabetMatchGameActivity` | Hiển thị bài học chữ cái và game ghép chữ cái |
| `CommunityFeedActivity`, `CreatePostActivity`, `HelpSupportActivity` | Bài đăng cộng đồng và hỗ trợ người dùng |

## 3. Code đáp ứng chức năng

### 3.1. Lớp/hàm liên quan

| File | Lớp/hàm | Giải thích |
|---|---|---|
| `child/QuizListActivity.java` | Luồng danh sách quiz | Hiển thị các bài trắc nghiệm |
| `child/QuizPlayActivity.java` | Logic quiz | Hiển thị câu hỏi, xử lý đáp án, tính điểm |
| `child/QuizHistoryActivity.java` | Tải lịch sử | Đọc lịch sử làm quiz |
| `data/repository/ContentRepository.java` | `getQuizQuestions()` | Tải câu hỏi quiz |
| `data/repository/ActivityAttemptRepository.java` | `startAttempt()` | Tạo lượt làm bài |
| `data/repository/ActivityAttemptRepository.java` | `completeAttempt()` | Lưu điểm và trạng thái hoàn thành |
| `data/repository/ActivityAttemptRepository.java` | `saveAnswer()` | Lưu đáp án chi tiết |
| `child/AiChatActivity.java` | `generateAiResponse()` | Gửi câu hỏi tới Gemini và hiển thị câu trả lời |
| `ai/GeminiService.java` | `generateResponse()` | Gọi Gemini API |
| `data/repository/AiRepository.java` | `createConversation()` | Tạo hội thoại AI |
| `data/repository/AiRepository.java` | `addMessage()` | Lưu tin nhắn của trẻ/AI |
| `data/repository/AiRepository.java` | `getMessages()` | Tải lịch sử tin nhắn |
| `child/AlphabetLearningActivity.java`, `child/AlphabetMatchGameActivity.java` | Luồng chữ cái | Hiển thị bài học chữ cái và lưu kết quả ghép chữ |
| `child/CommunityFeedActivity.java` | Luồng cộng đồng | Hiển thị bài đăng, lượt thích và bình luận |
| `child/CreatePostActivity.java` | Luồng tạo bài đăng | Tạo bài đăng và tải ảnh lên Firebase Storage |
| `common/HelpSupportActivity.java` | Màn hỗ trợ | Hiển thị nội dung trợ giúp người dùng |

### 3.2. Bảng/collection trong CSDL

| Collection | Mục đích |
|---|---|
| `content_catalog` | Danh mục nội dung quiz |
| `content_catalog/{contentId}/questions` | Câu hỏi quiz |
| `child_profiles/{childId}/activity_attempts` | Lịch sử làm quiz |
| `child_profiles/{childId}/activity_attempts/{attemptId}/answers` | Đáp án chi tiết |
| `ai_conversations` | Hội thoại AI |
| `ai_conversations/{conversationId}/messages` | Tin nhắn AI |
| `posts` | Bài đăng cộng đồng |
| `posts/{postId}/comments` | Bình luận bài đăng |

### 3.3. API gọi ngoài

| API | File sử dụng | Mục đích |
|---|---|---|
| Gemini API | `GeminiService`, `AiChatActivity` | Sinh phản hồi cho trợ lý AI |
| Cloud Firestore | `AiRepository`, `ContentRepository`, `ActivityAttemptRepository`, `QuizHistoryActivity` | Lưu quiz, kết quả làm bài và lịch sử AI |
| Firebase Storage | `CreatePostActivity` | Lưu ảnh bài đăng cộng đồng |

### 3.4. File code liên quan

| Nhóm | File |
|---|---|
| Quiz/trắc nghiệm | `child/QuizListActivity.java`, `child/QuizPlayActivity.java`, `child/QuizHistoryActivity.java`, `child/QuizHistoryAdapter.java` |
| Trợ lý AI | `child/AiChatActivity.java`, `ai/AiService.java`, `ai/GeminiService.java`, `data/repository/AiRepository.java` |
| Chức năng bổ sung không đánh số | `child/AlphabetLearningActivity.java`, `child/AlphabetMatchGameActivity.java`, `child/CommunityFeedActivity.java`, `child/CreatePostActivity.java`, `common/HelpSupportActivity.java` |
| Repository/model | `data/repository/ContentRepository.java`, `data/repository/ActivityAttemptRepository.java`, `data/model/Quiz.java`, `data/model/QuizQuestion.java`, `data/model/ActivityAttempt.java`, `data/model/AttemptAnswer.java`, `data/model/AiConversation.java`, `data/model/AiMessage.java`, `data/model/ContentCatalog.java` |

## 4. Hướng dẫn và lưu ý cài đặt, triển khai

- Cần cấu hình `GEMINI_API_KEY` trong `kid_app/local.properties`.
- Cần bật Cloud Firestore để lưu câu hỏi, kết quả quiz và hội thoại AI.
- Khi test AI, cần có kết nối mạng và API key hợp lệ.

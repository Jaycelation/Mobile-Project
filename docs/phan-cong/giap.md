# Tài liệu kỹ thuật cá nhân - Giáp

## 1. Danh sách chức năng được phân công

| STT | Nhóm chức năng | Phạm vi |
|---|---|---|
| 1 | Quiz/trắc nghiệm | Danh sách quiz, màn hình làm quiz, tính điểm và lịch sử làm bài |
| 2 | Trợ lý học tập AI | Tích hợp Gemini API để hỗ trợ trẻ học tập |
| 3 | Nội dung màu sắc/chữ cái | Luồng học màu sắc và chữ cái |
| 4 | Cộng đồng và hỗ trợ | Bài đăng cộng đồng và màn hình trợ giúp/hỗ trợ |

## 2. Kiến trúc chi tiết module

```text
QuizListActivity
  -> content_catalog/questions
  -> QuizPlayActivity
  -> activity_attempts
  -> QuizHistoryActivity

AiChatActivity
  -> GeminiService.generateResponse()
  -> Gemini API
  -> AiRepository
  -> ai_conversations/messages

CommunityFeedActivity / CreatePostActivity
  -> posts + posts/comments
  -> Firebase Storage để lưu ảnh
```

| Thành phần | Vai trò |
|---|---|
| `QuizListActivity` | Hiển thị danh sách quiz |
| `QuizPlayActivity` | Hiển thị câu hỏi, xử lý đáp án và tính điểm |
| `QuizHistoryActivity` | Hiển thị lịch sử làm quiz |
| `AiChatActivity` | Giao diện trò chuyện với AI |
| `GeminiService` | Gọi Gemini API |
| `AiRepository` | Lưu hội thoại và tin nhắn |
| `ContentRepository` | Tải danh mục nội dung và câu hỏi quiz |
| `ColorGameActivity`, `ColorMatchGameActivity` | Hoạt động học màu sắc |
| `AlphabetLearningActivity` | Hoạt động học chữ cái |
| `CommunityFeedActivity` | Hiển thị bài đăng cộng đồng |
| `CreatePostActivity` | Tạo bài đăng và tải ảnh lên |
| `HelpSupportActivity` | Màn hình trợ giúp/hỗ trợ |

## 3. Code đáp ứng chức năng

### 3.1. Lớp/hàm liên quan

| File | Lớp/hàm | Giải thích |
|---|---|---|
| `ai/GeminiService.java` | `generateResponse()` | Gọi Gemini API và trả về phản hồi được sinh |
| `data/repository/AiRepository.java` | `createConversation()` | Tạo hội thoại AI |
| `data/repository/AiRepository.java` | `addMessage()` | Lưu tin nhắn của trẻ/AI |
| `data/repository/AiRepository.java` | `getMessages()` | Tải lịch sử tin nhắn trong hội thoại |
| `data/repository/ContentRepository.java` | `getContentByType()` | Tải nội dung theo loại |
| `data/repository/ContentRepository.java` | `getQuizQuestions()` | Tải câu hỏi quiz |
| `child/AiChatActivity.java` | `generateAiResponse()` | Gửi prompt tới Gemini và hiển thị kết quả |
| `child/QuizPlayActivity.java` | Logic quiz | Hiển thị câu hỏi, nhận đáp án và tính điểm |
| `child/QuizHistoryActivity.java` | Tải lịch sử | Đọc `activity_attempts` để hiển thị lịch sử quiz |
| `child/CreatePostActivity.java` | Luồng tạo bài đăng | Tải ảnh lên và tạo document `posts` |
| `child/CommunityFeedActivity.java` | Luồng tải bảng tin | Đọc bài đăng, lượt thích và bình luận |

### 3.2. Bảng/collection trong CSDL

| Collection | Mục đích |
|---|---|
| `content_catalog` | Danh mục nội dung học tập và quiz |
| `content_catalog/{contentId}/questions` | Câu hỏi quiz |
| `child_profiles/{childId}/activity_attempts` | Kết quả quiz hoặc hoạt động học tập |
| `ai_conversations` | Phiên hội thoại AI |
| `ai_conversations/{conversationId}/messages` | Tin nhắn hội thoại AI |
| `posts` | Bài đăng cộng đồng |
| `posts/{postId}/comments` | Bình luận bài đăng |

### 3.3. API gọi ngoài

| API | File sử dụng | Mục đích |
|---|---|---|
| Gemini API | `GeminiService`, `AiChatActivity` | Sinh phản hồi cho trợ lý AI |
| Cloud Firestore | `AiRepository`, `ContentRepository`, `CommunityFeedActivity`, `QuizHistoryActivity` | Lưu nội dung, kết quả quiz, lịch sử AI và bài đăng |
| Firebase Storage | `CreatePostActivity` | Tải ảnh bài đăng cộng đồng lên |

### 3.4. File code liên quan

| Nhóm | File |
|---|---|
| Quiz | `child/QuizListActivity.java`, `child/QuizPlayActivity.java`, `child/QuizHistoryActivity.java`, `child/QuizHistoryAdapter.java` |
| AI | `child/AiChatActivity.java`, `ai/AiService.java`, `ai/GeminiService.java`, `data/repository/AiRepository.java` |
| Màu sắc/chữ cái | `child/ColorListActivity.java`, `child/ColorGameActivity.java`, `child/ColorMatchGameActivity.java`, `child/AlphabetLearningActivity.java`, `child/LearningListActivity.java` |
| Cộng đồng/hỗ trợ | `child/CommunityFeedActivity.java`, `child/CreatePostActivity.java`, `common/HelpSupportActivity.java` |
| Repository/model | `data/repository/ContentRepository.java`, `data/model/Quiz.java`, `data/model/QuizQuestion.java`, `data/model/AiConversation.java`, `data/model/AiMessage.java`, `data/model/ContentCatalog.java`, `data/model/ContentLevel.java`, `data/model/ColorActivity.java` |

## 4. Hướng dẫn và lưu ý cài đặt, triển khai

- Add `GEMINI_API_KEY` in `local.properties` or environment variables.
- Internet access is required for AI features.
- Quiz data must exist in `content_catalog` and subcollection `questions`.
- Chức năng cộng đồng cần collection Firestore `posts`; phần tải ảnh lên cần Firebase Storage.
- Do not hard-code API keys in source code.
- Build from `kid_app` with `./gradlew :app:assembleDebug`.
- Relevant classes and external API call sites include Vietnamese no-accent comments using `// Chuc nang: ...`.

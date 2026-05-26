# Kid App - Tài liệu kỹ thuật tổng quan

Repository: [https://github.com/Jaycelation/Mobile-Project](https://github.com/Jaycelation/Mobile-Project)

Kid App là ứng dụng Android hỗ trợ trẻ em học tập thông qua trò chơi giáo dục, câu đố/trắc nghiệm, học màu sắc, học số đếm, kết nối lớp học, theo dõi tiến độ, phản hồi/khen thưởng và trợ lý học tập AI. Dự án sử dụng Android Java/XML, Firebase Authentication, Cloud Firestore, Firebase Storage và Gemini API.

## 1. Phạm vi dự án

| Thành phần | Công nghệ sử dụng |
|---|---|
| Ứng dụng di động | Android Java, XML layout |
| Xác thực | Firebase Authentication |
| Cơ sở dữ liệu | Cloud Firestore |
| Lưu trữ tệp | Firebase Storage |
| Trợ lý AI | Gemini API |
| Kiểu kiến trúc | Activity, Service, Repository, Model |

## 2. Phân công chức năng và code liên quan

Phân công dưới đây bám theo bộ khung báo cáo cá nhân `khung_bao_cao_ca_nhan_md_v2`.

| MSSV | Thành viên | Chức năng chính | Activity/Screen chính | Repository/Service chính | Model/dữ liệu chính |
|---|---|---|---|---|---|
| B22DCAT251 | Đặng Đức Tài | Trò chơi giáo dục; phản hồi & khen thưởng | `GameListActivity`, `PatternGameActivity`, `FastEyeGameActivity`, `PuzzleGameActivity`, `BadgeCollectionActivity`, `LeaderboardActivity`, `ChildProgressActivity`, `ParentFeedbackActivity`, `ParentFeedbackListActivity` | `ActivityAttemptRepository`, `BadgeRepository`, `FeedbackRepository`, `ContentRepository` | `ContentCatalog`, `Game`, `ContentLevel`, `ActivityAttempt`, `Badge`, `ChildBadge`, `FeedbackNote`, `ChildStats`, `LeaderboardSnapshot` |
| B22DCAT103 | Nguyễn Hồng Giáp | Câu đố/trắc nghiệm; trợ lý AI | `QuizListActivity`, `QuizPlayActivity`, `QuizHistoryActivity`, `AiChatActivity` | `ContentRepository`, `ActivityAttemptRepository`, `AiRepository`, `GeminiService` | `ContentCatalog`, `Quiz`, `QuizQuestion`, `ActivityAttempt`, `AttemptAnswer`, `AiConversation`, `AiMessage` |
| B22DCAT159 | Nguyễn Thanh Kiên | Học số đếm; kết nối lớp học & giáo viên | `CountingListActivity`, `CountingGameActivity`, `CountingFruitGameActivity`, `NumberMatchGameActivity`, `CreateClassActivity`, `ClassManagementActivity`, `ClassDetailActivity`, `JoinClassActivity`, `AssignmentManagementActivity`, `CreateAssignmentActivity`, `ChildAssignmentActivity` | `ContentRepository`, `ActivityAttemptRepository`, `ClassRepository`, `AssignmentRepository` | `ContentCatalog`, `CountingActivity`, `ContentLevel`, `ActivityAttempt`, `AppClass`, `ClassMember`, `Assignment`, `AssignmentSubmission`, `LeaderboardSnapshot` |
| B22DCAT247 | Nguyễn Thanh Sơn | Học màu sắc; tài khoản, hồ sơ & cài đặt phụ huynh | `ColorListActivity`, `ColorGameActivity`, `ColorMatchGameActivity`, `SignInActivity`, `SignUpActivity`, `ForgotPasswordActivity`, `ParentHomeActivity`, `AddChildActivity`, `EditChildActivity`, `ChildSettingsActivity`, `ChildProfileActivity` | `AuthService`, `AccountRepository`, `ChildProfileService`, `ChildProfileRepository`, `ContentRepository`, `ActivityAttemptRepository` | `Account`, `ParentChildLink`, `ChildProfile`, `ChildSettings`, `ContentCatalog`, `ColorActivity`, `ContentLevel`, `ActivityAttempt` |

## 3. Kiến trúc hệ thống

```text
Người dùng
  -> Android Activity + XML Layout
  -> Service / Repository
  -> Firebase Auth / Firestore / Storage / Gemini API
  -> Java Model
  -> Hiển thị giao diện
```

| Tầng | Thành phần | Vai trò |
|---|---|---|
| UI | `auth`, `child`, `parent`, `teacher`, `admin` activities | Nhận thao tác người dùng, hiển thị dữ liệu, điều hướng |
| Common | `BaseActivity`, `AppConstants`, `KidLearnApp` | Tiện ích dùng chung, hằng số, khởi tạo ứng dụng |
| Service | `AuthService`, `ChildProfileService`, `GeminiService` | Đóng gói nghiệp vụ trung gian và API ngoài |
| Repository | `AccountRepository`, `ChildProfileRepository`, `ContentRepository`, `ActivityAttemptRepository`, `ClassRepository`, `AssignmentRepository`, `BadgeRepository`, `FeedbackRepository`, `AiRepository` | Đọc/ghi Firestore theo từng miền nghiệp vụ |
| Model | `Account`, `ChildProfile`, `ContentCatalog`, `ActivityAttempt`, `AppClass`, `Assignment`, `Badge`, `FeedbackNote`, `Quiz`, `AiConversation`, ... | Ánh xạ dữ liệu giữa Firestore và Java object |
| Backend/API | Firebase Auth, Firestore, Storage, Gemini API | Xác thực, lưu dữ liệu, lưu ảnh, sinh phản hồi AI |

## 4. Bảng API chức năng của dự án

Bảng dưới đây mô tả các nhóm API ở mức nghiệp vụ. Dự án không xây dựng REST server riêng; các API được triển khai bằng Firebase Auth, Cloud Firestore, Firebase Storage hoặc Gemini API thông qua Activity/Service/Repository.

| Nhóm API | Dữ liệu/API liên quan | Chức năng nghiệp vụ | Người phụ trách | Code triển khai chính |
|---|---|---|---|---|
| API xác thực và vai trò người dùng | Firebase Auth, `accounts` | Đăng ký, đăng nhập, đăng xuất, quên mật khẩu, điều hướng theo vai trò | Sơn | `AuthService`, `SignInActivity`, `SignUpActivity`, `ForgotPasswordActivity`, `AccountRepository` |
| API hồ sơ trẻ và cài đặt phụ huynh | `child_profiles`, `parent_child_links`, `child_profiles/{childId}/settings` | Tạo/sửa hồ sơ trẻ, chọn hồ sơ trẻ, lưu cài đặt học tập và giới hạn sử dụng | Sơn | `ChildProfileService`, `ChildProfileRepository`, `ParentHomeActivity`, `AddChildActivity`, `EditChildActivity`, `ChildSettingsActivity` |
| API học màu sắc | `content_catalog`, `content_catalog/{contentId}/levels`, `activity_attempts` | Hiển thị bài học màu sắc, xử lý tương tác chọn/kéo thả và lưu kết quả | Sơn | `ColorListActivity`, `ColorGameActivity`, `ColorMatchGameActivity`, `ContentRepository`, `ActivityAttemptRepository` |
| API trò chơi giáo dục | `content_catalog`, `content_catalog/{contentId}/levels`, `activity_attempts`, `answers` | Chơi game tư duy/phản xạ, lưu đáp án, điểm số và trạng thái hoàn thành | Tài | `GameListActivity`, `PatternGameActivity`, `FastEyeGameActivity`, `PuzzleGameActivity`, `ActivityAttemptRepository` |
| API phản hồi và khen thưởng | `badges`, `child_badges`, `child_stats`, `feedback_notes`, `leaderboard_snapshots` | Cộng điểm, trao huy hiệu, xem tiến độ, bảng xếp hạng và phản hồi/khen thưởng | Tài | `BadgeRepository`, `FeedbackRepository`, `ChildProgressActivity`, `BadgeCollectionActivity`, `LeaderboardActivity`, `ParentFeedbackActivity` |
| API quiz/trắc nghiệm | `content_catalog/{contentId}/questions`, `activity_attempts`, `answers` | Hiển thị quiz, xử lý đáp án, tính điểm và lưu lịch sử làm bài | Giáp | `QuizListActivity`, `QuizPlayActivity`, `QuizHistoryActivity`, `ContentRepository`, `ActivityAttemptRepository` |
| API trợ lý học tập AI | Gemini API, `ai_conversations`, `ai_conversations/{conversationId}/messages` | Gửi câu hỏi của trẻ tới Gemini, nhận phản hồi, lưu hội thoại và tin nhắn | Giáp | `GeminiService`, `AiRepository`, `AiChatActivity` |
| API học số đếm | `content_catalog`, `content_catalog/{contentId}/levels`, `activity_attempts` | Hiển thị hoạt động số đếm, xử lý âm thanh/sự kiện chạm và lưu kết quả | Kiên | `CountingListActivity`, `CountingGameActivity`, `CountingFruitGameActivity`, `NumberMatchGameActivity`, `ActivityAttemptRepository` |
| API lớp học và giáo viên | `classes`, `class_members`, `assignments`, `assignment_submissions` | Giáo viên tạo lớp, cấp mã tham gia, phụ huynh cho trẻ tham gia lớp, giáo viên giao bài và xem bài nộp | Kiên | `ClassRepository`, `AssignmentRepository`, `CreateClassActivity`, `ClassManagementActivity`, `ClassDetailActivity`, `JoinClassActivity`, `CreateAssignmentActivity`, `ChildAssignmentActivity` |
| API bài đăng cộng đồng | `posts`, `posts/{postId}/comments`, Firebase Storage | Tạo bài đăng cộng đồng, hiển thị bảng tin, thích/bình luận, tải ảnh bài đăng lên | Chức năng phụ ngoài khung báo cáo cá nhân chính | `CommunityFeedActivity`, `CreatePostActivity` |

### 4.1. API nghiệp vụ tiêu biểu

| API nghiệp vụ | Đầu vào chính | Đầu ra chính | Triển khai hiện tại | Phụ trách |
|---|---|---|---|---|
| `signIn(email, password)` | Email, mật khẩu | Phiên đăng nhập và vai trò | Firebase Auth + `accounts` qua `AuthService` | Sơn |
| `createChildProfile(parentId, profile)` | Mã phụ huynh, dữ liệu hồ sơ trẻ | Hồ sơ trẻ, liên kết phụ huynh-trẻ, thống kê ban đầu | `ChildProfileService` + `ChildProfileRepository` | Sơn |
| `saveChildSettings(childId, settings)` | Mã trẻ, cài đặt học tập | Cài đặt đã lưu | `ChildProfileRepository.saveChildSettings()` | Sơn |
| `loadColorContent(contentId)` | Mã nội dung màu sắc | Bài học/level màu sắc | `ContentRepository` + color activities | Sơn |
| `startGameAttempt(childId, contentId)` | Mã trẻ, mã game | Lượt chơi mới | `ActivityAttemptRepository.startAttempt()` | Tài |
| `completeGameAttempt(childId, attemptId, score, status)` | Mã trẻ, lượt chơi, điểm, trạng thái | Kết quả trò chơi được lưu | `ActivityAttemptRepository.completeAttempt()` | Tài |
| `awardBadge(childId, badgeId)` | Mã trẻ, mã huy hiệu | Huy hiệu đã trao | `BadgeRepository.awardBadge()` | Tài |
| `getFeedbackForChild(childId)` | Mã trẻ | Danh sách phản hồi/khen thưởng | `FeedbackRepository.getFeedbackForChild()` | Tài |
| `getQuizQuestions(contentId)` | Mã nội dung quiz | Danh sách câu hỏi | `ContentRepository.getQuizQuestions()` | Giáp |
| `submitQuizAttempt(childId, attemptId, answers)` | Mã trẻ, lượt làm bài, đáp án | Điểm và lịch sử làm bài | `QuizPlayActivity` + `ActivityAttemptRepository` | Giáp |
| `assistantChat(childId, message)` | Mã trẻ, câu hỏi | Câu trả lời từ AI | `AiChatActivity` + `GeminiService` | Giáp |
| `loadCountingContent(contentId)` | Mã nội dung số đếm | Bài học/level số đếm | `ContentRepository` + counting activities | Kiên |
| `joinClassByCode(joinCode, childId)` | Mã tham gia, mã trẻ | Trẻ được thêm vào lớp | `JoinClassActivity` + `ClassRepository` | Kiên |
| `createAssignment(classId, contentId)` | Mã lớp, nội dung/bài học | Bài tập mới cho lớp | `CreateAssignmentActivity` + `AssignmentRepository` | Kiên |
| `submitAssignment(assignmentId, childId, score)` | Mã bài tập, mã trẻ, điểm | Bài nộp được cập nhật | `AssignmentRepository.submitAssignment()` | Kiên |

### 4.2. API chi tiết

| Mã API | Thao tác | Endpoint/đường dẫn dữ liệu | Request chính | Response chính | File xử lý | Phụ trách |
|---|---|---|---|---|---|---|
| AUTH-01 | POST | Firebase Auth `signInWithEmailAndPassword` | `email`, `password` | Phiên đăng nhập, `uid` | `AuthService.signIn()` | Sơn |
| AUTH-02 | POST | Firebase Auth + `/accounts/{uid}` | `email`, `password`, `fullName`, `role` | Tài khoản mới | `AuthService.signUp()` | Sơn |
| AUTH-03 | POST | Firebase Auth `sendPasswordResetEmail` | `email` | Email đặt lại mật khẩu | `AuthService.sendPasswordResetEmail()` | Sơn |
| CHILD-01 | POST | `/child_profiles/{childId}` + `/parent_child_links/{linkId}` | `parentId`, hồ sơ trẻ | Hồ sơ trẻ, liên kết phụ huynh-trẻ, thống kê ban đầu | `ChildProfileService.createChildProfile()` | Sơn |
| CHILD-02 | PATCH | `/child_profiles/{childId}` | Dữ liệu hồ sơ trẻ | Hồ sơ trẻ đã cập nhật | `ChildProfileRepository.updateChildProfile()` | Sơn |
| CHILD-03 | PUT | `/child_profiles/{childId}/settings/child_settings` | Cài đặt học tập, giới hạn sử dụng | Cài đặt đã lưu | `ChildProfileRepository.saveChildSettings()` | Sơn |
| COLOR-01 | GET | `/content_catalog`, `/content_catalog/{contentId}/levels` | `contentType=color`, `ageGroup` | Nội dung/level màu sắc | `ContentRepository`, `ColorListActivity` | Sơn |
| COLOR-02 | POST/PATCH | `/child_profiles/{childId}/activity_attempts` | `childId`, đáp án, điểm | Kết quả hoạt động màu sắc | `ColorGameActivity`, `ColorMatchGameActivity`, `ActivityAttemptRepository` | Sơn |
| GAME-01 | GET | `/content_catalog`, `/content_catalog/{contentId}/levels` | `contentType=game`, `ageGroup` | Danh sách game/level | `GameListActivity`, `ContentRepository` | Tài |
| GAME-02 | POST | `/child_profiles/{childId}/activity_attempts` | `childId`, `contentId`, thời gian bắt đầu | `attemptId` | `ActivityAttemptRepository.startAttempt()` | Tài |
| GAME-03 | PATCH | `/child_profiles/{childId}/activity_attempts/{attemptId}` | `score`, `status`, `completedAt` | Kết quả trò chơi | `ActivityAttemptRepository.completeAttempt()` | Tài |
| REWARD-01 | POST | `/child_badges` | `childId`, `badgeId`, nguồn nhận huy hiệu | Huy hiệu đã trao | `BadgeRepository.awardBadge()` | Tài |
| REWARD-02 | GET | `/leaderboard_snapshots`, `/child_stats` | `classId`, `childId`, `periodType` | Tiến độ/bảng xếp hạng | `FeedbackRepository`, `ChildProgressActivity`, `LeaderboardActivity` | Tài |
| FEEDBACK-01 | POST/GET | `/feedback_notes` | `teacherId`, `childId`, nội dung phản hồi | Phản hồi/khen thưởng | `FeedbackRepository`, `ParentFeedbackActivity` | Tài |
| QUIZ-01 | GET | `/content_catalog/{contentId}/questions` | `contentId` | Danh sách câu hỏi quiz | `ContentRepository.getQuizQuestions()` | Giáp |
| QUIZ-02 | POST/PATCH | `/child_profiles/{childId}/activity_attempts` | `answers`, `score`, `status` | Kết quả làm quiz | `QuizPlayActivity`, `ActivityAttemptRepository` | Giáp |
| AI-01 | POST | Gemini API | `prompt`, ngữ cảnh học tập | Câu trả lời AI | `GeminiService.generateResponse()` | Giáp |
| AI-02 | POST/GET | `/ai_conversations/{conversationId}/messages` | `childId`, `message`, `senderType` | Lịch sử tin nhắn AI | `AiRepository.addMessage()`, `AiRepository.getMessages()` | Giáp |
| COUNT-01 | GET | `/content_catalog`, `/content_catalog/{contentId}/levels` | `contentType=counting`, `ageGroup` | Nội dung/level số đếm | `ContentRepository`, `CountingListActivity` | Kiên |
| COUNT-02 | POST/PATCH | `/child_profiles/{childId}/activity_attempts` | `childId`, đáp án, điểm | Kết quả học số đếm | `CountingGameActivity`, `CountingFruitGameActivity`, `NumberMatchGameActivity` | Kiên |
| CLASS-01 | POST | `/classes` | `teacherId`, `className`, `joinCode` | `classId` | `ClassRepository.createClass()` | Kiên |
| CLASS-02 | GET | `/classes?joinCode={joinCode}` | `joinCode` | Thông tin lớp học | `ClassRepository.getClassByJoinCode()` | Kiên |
| CLASS-03 | POST | `/class_members` | `classId`, `childId`, `joinedAt` | Thành viên lớp | `ClassRepository.addMember()` | Kiên |
| ASSIGN-01 | POST | `/assignments` | `classId`, `teacherId`, nội dung bài tập | `assignmentId` | `AssignmentRepository.createAssignment()` | Kiên |
| ASSIGN-02 | GET | `/assignments?classId={classId}` | `classId` | Danh sách bài tập của lớp | `AssignmentRepository.getAssignmentsByClass()` | Kiên |
| ASSIGN-03 | POST/PATCH | `/assignment_submissions` | `assignmentId`, `childId`, `score`, `latestAttemptId` | Bài nộp đã cập nhật | `AssignmentRepository.submitAssignment()` | Kiên |

## 5. Bảng dữ liệu trong Firestore

| Collection | Mục đích | Phạm vi chính |
|---|---|---|
| `accounts` | Tài khoản, email, họ tên, vai trò | Sơn |
| `child_profiles` | Hồ sơ trẻ | Sơn |
| `parent_child_links` | Liên kết phụ huynh - trẻ | Sơn |
| `child_profiles/{childId}/settings` | Cài đặt học tập của trẻ | Sơn |
| `content_catalog` | Danh mục nội dung học tập | Sơn, Tài, Kiên, Giáp |
| `content_catalog/{contentId}/levels` | Level của nội dung học/game | Sơn, Tài, Kiên |
| `content_catalog/{contentId}/questions` | Câu hỏi quiz | Giáp |
| `child_profiles/{childId}/activity_attempts` | Lịch sử làm bài/chơi game/học màu/học số đếm | Sơn, Tài, Kiên, Giáp |
| `child_profiles/{childId}/activity_attempts/{attemptId}/answers` | Câu trả lời chi tiết | Tài, Giáp |
| `child_stats` | Điểm, chuỗi học liên tiếp, tiến độ | Tài |
| `badges` | Danh mục huy hiệu | Tài |
| `child_badges` | Huy hiệu trẻ đã nhận | Tài |
| `feedback_notes` | Phiên phản hồi/khen thưởng | Tài |
| `leaderboard_snapshots` | Dữ liệu bảng xếp hạng | Tài |
| `ai_conversations` | Hội thoại AI | Giáp |
| `ai_conversations/{conversationId}/messages` | Tin nhắn AI | Giáp |
| `classes` | Lớp học | Kiên |
| `class_members` | Thành viên lớp | Kiên |
| `assignments` | Bài tập giáo viên giao | Kiên |
| `assignment_submissions` | Bài nộp của trẻ | Kiên |
| `posts` | Bài đăng cộng đồng | Chức năng phụ |
| `posts/{postId}/comments` | Bình luận bài đăng | Chức năng phụ |

## 6. API gọi ngoài

| API gọi ngoài | File sử dụng | Mục đích |
|---|---|---|
| Firebase Authentication | `AuthService`, `SignInActivity`, `ForgotPasswordActivity` | Đăng nhập, đăng ký, đặt lại mật khẩu |
| Cloud Firestore | Các repository và một số activity liên quan | Lưu/đọc dữ liệu nghiệp vụ |
| Firebase Storage | `CreatePostActivity`, `FirestoreHelper` | Lưu ảnh bài đăng cộng đồng nếu dùng chức năng phụ |
| Gemini API | `GeminiService`, `AiChatActivity` | Sinh câu trả lời cho trợ lý AI |

## 7. Hướng dẫn cài đặt và triển khai

1. Clone repository:

```bash
git clone https://github.com/Jaycelation/Mobile-Project.git
```

2. Mở thư mục `kid_app` bằng Android Studio.

3. Cấu hình `kid_app/local.properties`:

```properties
GEMINI_API_KEY=your_api_key_here
```

4. Cấu hình Firebase:

- Bật Firebase Authentication với phương thức Email/Password.
- Tạo Cloud Firestore.
- Tạo Firebase Storage nếu sử dụng chức năng bài đăng cộng đồng có ảnh.
- Đặt `google-services.json` vào `kid_app/app/` khi build local. File này không nên commit công khai.

5. Build ứng dụng:

```bash
cd kid_app
./gradlew :app:assembleDebug
```

Trên Windows, nên dùng đường dẫn không dấu như `C:\Projects\Mobile-Project` để tránh cảnh báo đường dẫn của Android Gradle Plugin.

## 8. Ghi chú tài liệu hóa code

- Các service, repository và vị trí gọi API ngoài quan trọng đã có comment tiếng Việt không dấu theo dạng `// Chuc nang: ...`.
- Nhánh `release` chỉ giữ code ứng dụng trong `kid_app`, `README.md` và `.gitignore`.
- Cấu hình local, file sinh tự động, crash log và bản nháp báo cáo được loại khỏi Git bằng `.gitignore`.

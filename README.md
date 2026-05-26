# Kid App - Tài liệu kỹ thuật tổng quan

Repository: [https://github.com/Jaycelation/Mobile-Project](https://github.com/Jaycelation/Mobile-Project)

Kid App là ứng dụng Android hỗ trợ trẻ em học tập thông qua trò chơi, quiz, lớp học, bài tập, thống kê tiến độ, huy hiệu, phản hồi giáo viên và trợ lý AI. Dự án sử dụng Android Java/XML, Firebase Authentication, Cloud Firestore, Firebase Storage và Gemini API.

## 1. Phạm vi dự án

| Thành phần | Công nghệ sử dụng |
|---|---|
| Ứng dụng di động | Android Java, XML layout |
| Xác thực | Firebase Authentication |
| Cơ sở dữ liệu | Cloud Firestore |
| Lưu trữ tệp | Firebase Storage |
| Trợ lý AI | Gemini API |
| Kiểu kiến trúc | Activity, Service, Repository, Model |

## 2. Phân công chức năng

| Thành viên | Phạm vi phụ trách | Tài liệu chi tiết |
|---|---|---|
| Sơn | Xác thực, phân quyền, hồ sơ trẻ/phụ huynh, quản trị hệ thống, khung dùng chung | [docs/phan-cong/son.md](docs/phan-cong/son.md) |
| Kiên | Lớp học, đăng ký lớp, giao bài tập, xem tiến độ lớp, thông báo giáo viên, module số đếm | [docs/phan-cong/kien.md](docs/phan-cong/kien.md) |
| Tài | Trò chơi tư duy/phản xạ, ghép hình/nhận biết, tiến độ học tập, huy hiệu, bảng xếp hạng | [docs/phan-cong/tai.md](docs/phan-cong/tai.md) |
| Giáp | Quiz/trắc nghiệm, trợ lý AI, nội dung màu sắc/chữ cái, cộng đồng và hỗ trợ | [docs/phan-cong/giap.md](docs/phan-cong/giap.md) |

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
| Repository | `AccountRepository`, `ChildProfileRepository`, `ClassRepository`, `AssignmentRepository`, `ActivityAttemptRepository`, `BadgeRepository`, `AiRepository`, `ContentRepository`, `FeedbackRepository` | Đọc/ghi Firestore theo từng miền nghiệp vụ |
| Model | `Account`, `ChildProfile`, `AppClass`, `Assignment`, `ActivityAttempt`, `Badge`, `ChildStats`, `Quiz`, `AiConversation`, ... | Ánh xạ dữ liệu giữa Firestore và Java object |
| Backend/API | Firebase Auth, Firestore, Storage, Gemini API | Xác thực, lưu dữ liệu, lưu ảnh, sinh phản hồi AI |

## 4. Bảng API chức năng của dự án

Bảng dưới đây mô tả các nhóm API ở mức nghiệp vụ, bám theo thiết kế ban đầu của hệ thống. Trong code hiện tại, một số API được triển khai bằng repository/service, một số luồng đơn giản được gọi trực tiếp từ Activity tới Firebase.

| Nhóm API | Dữ liệu/API liên quan | Chức năng nghiệp vụ | Người phụ trách | Code triển khai chính |
|---|---|---|---|---|
| 1. API xác thực và vai trò người dùng | Firebase Auth, `accounts` | Đăng ký, đăng nhập, đăng xuất, quên mật khẩu, tạo tài khoản người dùng và điều hướng theo vai trò | Sơn | `AuthService`, `SignInActivity`, `SignUpActivity`, `ForgotPasswordActivity`, `AccountRepository` |
| 2. API hồ sơ trẻ và cài đặt | `child_profiles`, `parent_child_links`, `child_profiles/{childId}/settings` | Tạo/sửa hồ sơ trẻ, chọn hồ sơ trẻ, lưu cài đặt học tập và giới hạn sử dụng | Sơn | `ChildProfileService`, `ChildProfileRepository`, `ParentHomeActivity`, `AddChildActivity`, `EditChildActivity`, `ChildSettingsActivity` |
| 3. API danh mục nội dung học tập | `content_catalog`, `content_catalog/{contentId}/levels`, `content_catalog/{contentId}/questions`, Firebase Storage | Quản lý danh mục nội dung học tập, câu hỏi quiz, dữ liệu bài học, hình ảnh/tài nguyên liên quan | Giáp, Sơn | `ContentRepository`, `ContentManagementActivity`, `LearningListActivity`, `QuizListActivity` |
| 4. API hoạt động và kết quả trò chơi | `child_profiles/{childId}/activity_attempts`, `answers`, `assignment_submissions` | Tạo lượt chơi/lượt học, lưu đáp án, điểm số, thời gian và trạng thái hoàn thành | Tài, Kiên, Giáp | `ActivityAttemptRepository`, các `*GameActivity`, `QuizPlayActivity`, `ChildAssignmentActivity` |
| 5. API huy hiệu và phần thưởng | `child_stats`, `badges`, `child_badges` | Cộng điểm, cập nhật tiến độ, kiểm tra điều kiện nhận huy hiệu và lưu huy hiệu của trẻ | Tài | `ChildProfileRepository`, `BadgeRepository`, `BadgeCollectionActivity`, `ChildProgressActivity` |
| 6. API quiz và lượt làm bài | `content_catalog/{contentId}/questions`, `activity_attempts` | Hiển thị quiz, xử lý chọn đáp án, tính điểm, lưu lịch sử làm bài | Giáp | `QuizListActivity`, `QuizPlayActivity`, `QuizHistoryActivity`, `ContentRepository`, `ActivityAttemptRepository` |
| 7. API lớp học và bài tập | `classes`, `class_members`, `assignments`, `assignment_submissions` | Giáo viên tạo lớp, cấp mã tham gia, phụ huynh đăng ký lớp cho trẻ, giáo viên giao bài và xem bài nộp | Kiên | `ClassRepository`, `AssignmentRepository`, `ClassManagementActivity`, `CreateClassActivity`, `JoinClassActivity`, `CreateAssignmentActivity`, `AssignmentDetailActivity` |
| 8. API trợ lý học tập AI | Gemini API, `ai_conversations`, `ai_conversations/{conversationId}/messages` | Gửi câu hỏi của trẻ tới Gemini, nhận phản hồi, lưu hội thoại và tin nhắn | Giáp | `GeminiService`, `AiRepository`, `AiChatActivity` |
| 9. API thống kê tiến độ | `child_stats`, `activity_attempts`, `child_badges`, `leaderboard_snapshots`, `class_members` | Tổng hợp điểm, tiến độ, huy hiệu và bảng xếp hạng của trẻ/lớp | Tài | `ChildProgressActivity`, `BadgeCollectionActivity`, `LeaderboardActivity`, `FeedbackRepository` |
| 10. API phản hồi và thông báo | `feedback_notes`, `feedback_notes/{feedbackId}/messages`, `assignment_submissions`, `child_badges` | Giáo viên phản hồi cho phụ huynh, trao đổi tin nhắn, xem thông báo bài nộp/huy hiệu | Kiên | `FeedbackRepository`, `FeedbackListActivity`, `FeedbackChatActivity`, `ParentFeedbackActivity`, `TeacherNotificationActivity` |
| 11. API bài đăng cộng đồng | `posts`, `posts/{postId}/comments`, Firebase Storage | Tạo bài đăng cộng đồng, hiển thị bảng tin, thích/bình luận, tải ảnh bài đăng lên | Giáp | `CommunityFeedActivity`, `CreatePostActivity` |

### 4.1. API nghiệp vụ tiêu biểu

| API nghiệp vụ | Đầu vào chính | Đầu ra chính | Triển khai hiện tại |
|---|---|---|---|
| `signIn(email, password)` | Email, mật khẩu | Thông tin phiên đăng nhập và vai trò | Firebase Auth + `accounts` qua `AuthService` |
| `createChildProfile(parentId, profile)` | Mã phụ huynh, dữ liệu hồ sơ trẻ | Hồ sơ trẻ, liên kết phụ huynh-trẻ, thống kê ban đầu | `ChildProfileService` + `ChildProfileRepository` |
| `startAttempt(childId, contentId)` | Mã trẻ, mã nội dung | Một lượt học/chơi mới | `ActivityAttemptRepository.startAttempt()` |
| `completeAttempt(childId, attemptId, score, status)` | Mã trẻ, lượt học, điểm, trạng thái | Kết quả hoàn thành được lưu | `ActivityAttemptRepository.completeAttempt()` |
| `joinClassByCode(joinCode, childId)` | Mã tham gia, mã trẻ | Trẻ được thêm vào lớp | `JoinClassActivity` + `classes` + `class_members` |
| `createAssignment(classId, contentId)` | Mã lớp, nội dung/bài học | Bài tập mới cho lớp | `CreateAssignmentActivity` + `AssignmentRepository` |
| `submitAssignment(assignmentId, childId, score)` | Mã bài tập, mã trẻ, điểm | Bài nộp được cập nhật | `AssignmentRepository` hoặc activity trò chơi |
| `assistantChat(childId, message)` | Mã trẻ, nội dung câu hỏi | Câu trả lời từ AI | `AiChatActivity` + `GeminiService` |
| `getProgressSummary(childId)` | Mã trẻ | Điểm, chuỗi học liên tiếp, bài hoàn thành, huy hiệu | `ChildProgressActivity` + `ChildProfileRepository` |

### 4.2. API chi tiết

Dự án không xây dựng REST server riêng. Các API dưới đây là các luồng nghiệp vụ mà ứng dụng Android gọi tới Firebase Auth, Cloud Firestore, Firebase Storage hoặc Gemini API thông qua service/repository. Bảng được trình bày theo dạng rút gọn giống API Doc/Swagger.

| Mã API | Thao tác | Endpoint/đường dẫn dữ liệu | Request chính | Response chính | File xử lý |
|---|---|---|---|---|---|
| AUTH-01 | POST | Firebase Auth `signInWithEmailAndPassword` | `email`, `password` | Phiên đăng nhập, `uid` | `AuthService.signIn()` |
| AUTH-02 | POST | Firebase Auth + `/accounts/{uid}` | `email`, `password`, `fullName`, `role` | Tài khoản mới | `AuthService.signUp()` |
| AUTH-03 | POST | Firebase Auth `sendPasswordResetEmail` | `email` | Email đặt lại mật khẩu | `AuthService.sendPasswordResetEmail()` |
| ACC-01 | GET | `/accounts/{uid}` | `uid` | Thông tin tài khoản và vai trò | `AccountRepository.getAccount()` |
| CHILD-01 | POST | `/child_profiles/{childId}` + `/parent_child_links/{linkId}` | `parentId`, hồ sơ trẻ | Hồ sơ trẻ, liên kết phụ huynh-trẻ, thống kê ban đầu | `ChildProfileService.createChildProfile()` |
| CHILD-02 | PATCH | `/child_profiles/{childId}` | Dữ liệu hồ sơ trẻ | Hồ sơ trẻ đã cập nhật | `ChildProfileRepository.updateChildProfile()` |
| CHILD-03 | PUT | `/child_profiles/{childId}/settings/child_settings` | Cài đặt học tập, giới hạn sử dụng | Cài đặt đã lưu | `ChildProfileRepository.saveChildSettings()` |
| CONTENT-01 | GET | `/content_catalog` | `contentType`, `ageGroup`, `status` | Danh sách nội dung học tập | `ContentRepository.getContentByTypeAndAge()` |
| QUIZ-01 | GET | `/content_catalog/{contentId}/questions` | `contentId` | Danh sách câu hỏi quiz | `ContentRepository.getQuizQuestions()` |
| ATTEMPT-01 | POST | `/child_profiles/{childId}/activity_attempts` | `contentId`, `activityType`, thời gian bắt đầu | `attemptId` | `ActivityAttemptRepository.startAttempt()` |
| ATTEMPT-02 | PATCH | `/child_profiles/{childId}/activity_attempts/{attemptId}` | `score`, `status`, `completedAt` | Kết quả lượt học/chơi | `ActivityAttemptRepository.completeAttempt()` |
| ATTEMPT-03 | POST | `/child_profiles/{childId}/activity_attempts/{attemptId}/answers` | Câu trả lời chi tiết | `answerId` | `ActivityAttemptRepository.saveAnswer()` |
| STATS-01 | PATCH | `/child_stats/{childId}` | `points`, bài hoàn thành | Điểm và tiến độ mới | `ChildProfileRepository.addPoints()` |
| BADGE-01 | GET | `/badges`, `/child_badges` | `childId`, `badgeId` nếu cần | Danh sách huy hiệu | `BadgeRepository.getAllBadges()`, `getChildBadges()` |
| BADGE-02 | POST | `/child_badges` | `childId`, `badgeId`, nguồn nhận huy hiệu | Huy hiệu đã trao | `BadgeRepository.awardBadge()` |
| CLASS-01 | POST | `/classes` | `teacherId`, `className`, `joinCode` | `classId` | `ClassRepository.createClass()` |
| CLASS-02 | GET | `/classes?joinCode={joinCode}` | `joinCode` | Thông tin lớp học | `ClassRepository.getClassByJoinCode()` |
| CLASS-03 | POST | `/class_members` | `classId`, `childId`, `joinedAt` | Thành viên lớp | `ClassRepository.addMember()` |
| ASSIGN-01 | POST | `/assignments` | `classId`, `teacherId`, nội dung bài tập | `assignmentId` | `AssignmentRepository.createAssignment()` |
| ASSIGN-02 | GET | `/assignments?classId={classId}` | `classId` | Danh sách bài tập của lớp | `AssignmentRepository.getAssignmentsByClass()` |
| ASSIGN-03 | POST/PATCH | `/assignment_submissions` | `assignmentId`, `childId`, `score`, `latestAttemptId` | Bài nộp đã cập nhật | `AssignmentRepository.submitAssignment()` |
| AI-01 | POST | Gemini API | `prompt`, ngữ cảnh học tập | Câu trả lời AI | `GeminiService.generateResponse()` |
| AI-02 | POST/GET | `/ai_conversations/{conversationId}/messages` | `childId`, `message`, `senderType` | Lịch sử tin nhắn AI | `AiRepository.addMessage()`, `getMessages()` |
| FEEDBACK-01 | POST/GET | `/feedback_notes` | `teacherId`, `childId`, `assignmentId`, nội dung phản hồi | Phản hồi giáo viên-phụ huynh | `FeedbackRepository.createFeedback()`, `getFeedbackForChild()` |
| RANK-01 | POST/GET | `/leaderboard_snapshots` | `classId`, `periodType`, danh sách điểm | Bảng xếp hạng | `FeedbackRepository.saveSnapshot()`, `getLatestSnapshot()` |
| POST-01 | POST | Firebase Storage `posts/{fileId}` + `/posts` | Nội dung bài đăng, ảnh nếu có | `postId`, `imageUrl` | `CreatePostActivity` |
| POST-02 | POST | `/posts/{postId}/comments` | `postId`, `commentText`, `authorId` | Bình luận mới | `CommunityFeedActivity` |

## 5. Bảng dữ liệu trong Firestore

| Collection | Mục đích |
|---|---|
| `accounts` | Tài khoản, email, họ tên, vai trò |
| `child_profiles` | Hồ sơ trẻ |
| `parent_child_links` | Liên kết phụ huynh - trẻ |
| `child_profiles/{childId}/settings` | Cài đặt học tập của trẻ |
| `child_profiles/{childId}/activity_attempts` | Lịch sử làm bài/chơi game |
| `child_profiles/{childId}/activity_attempts/{attemptId}/answers` | Câu trả lời chi tiết |
| `child_stats` | Điểm, chuỗi học liên tiếp, tiến độ |
| `badges` | Danh mục huy hiệu |
| `child_badges` | Huy hiệu trẻ đã nhận |
| `classes` | Lớp học |
| `class_members` | Thành viên lớp |
| `assignments` | Bài tập giáo viên giao |
| `assignment_submissions` | Bài nộp của trẻ |
| `feedback_notes` | Phiên phản hồi giáo viên - phụ huynh |
| `feedback_notes/{feedbackId}/messages` | Tin nhắn phản hồi |
| `content_catalog` | Danh mục nội dung học tập |
| `content_catalog/{contentId}/questions` | Câu hỏi quiz |
| `ai_conversations` | Hội thoại AI |
| `ai_conversations/{conversationId}/messages` | Tin nhắn AI |
| `posts` | Bài đăng cộng đồng |
| `posts/{postId}/comments` | Bình luận bài đăng |
| `leaderboard_snapshots` | Dữ liệu bảng xếp hạng |

## 6. API gọi ngoài

| API gọi ngoài | File sử dụng | Mục đích |
|---|---|---|
| Firebase Authentication | `AuthService`, `SignInActivity`, `ForgotPasswordActivity` | Đăng nhập, đăng ký, đặt lại mật khẩu |
| Cloud Firestore | Các repository và một số activity liên quan | Lưu/đọc dữ liệu nghiệp vụ |
| Firebase Storage | `CreatePostActivity`, `FirestoreHelper` | Lưu ảnh bài đăng cộng đồng |
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
- Tài liệu kỹ thuật cá nhân nằm trong `docs/phan-cong/`.
- Cấu hình local, file sinh tự động, crash log và bản nháp báo cáo được loại khỏi Git bằng `.gitignore`.

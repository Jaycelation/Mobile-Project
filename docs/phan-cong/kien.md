# Tài liệu kỹ thuật cá nhân - Nguyễn Thanh Kiên

## 1. Danh sách chức năng được phân công

| Mục | Chức năng | Nhiệm vụ cá nhân |
|---|---|---|
| 2.1.4 | Hoạt động học số đếm | Xử lý đồng bộ âm thanh đọc số và sự kiện chạm vào vật thể |
| 2.1.7 | Kết nối lớp học và giáo viên | Xử lý logic tạo mã lớp và kết nối dữ liệu giáo viên - học sinh |

### Chức năng bổ sung không đánh số

| Chức năng/code bổ sung | File/Class liên quan | Ghi chú |
|---|---|---|
| Màn hình giáo viên, hồ sơ giáo viên, thông báo và phản hồi phía giáo viên | `TeacherHomeActivity`, `TeacherProfileActivity`, `EditTeacherProfileActivity`, `TeacherNotificationActivity`, `NotificationSettingsActivity`, `FeedbackListActivity`, `FeedbackChatActivity`, `AssignmentDetailActivity`, `AddStudentActivity` | Có code trong dự án nhưng không gán thêm chỉ mục mới |

## 2. Kiến trúc chi tiết module

```text
CountingListActivity
  -> CountingGameActivity / CountingFruitGameActivity / NumberMatchGameActivity
  -> ContentRepository / ActivityAttemptRepository

Teacher class flow
  -> CreateClassActivity / ClassManagementActivity / ClassDetailActivity
  -> ClassRepository

Assignment flow
  -> AssignmentManagementActivity / CreateAssignmentActivity / AssignmentDetailActivity / ChildAssignmentActivity
  -> AssignmentRepository

Parent class join
  -> JoinClassActivity
  -> ClassRepository

Teacher support flow
  -> TeacherHomeActivity / TeacherProfileActivity / TeacherNotificationActivity
  -> FeedbackListActivity / FeedbackChatActivity / NotificationSettingsActivity
```

| Thành phần | Vai trò |
|---|---|
| `CountingListActivity` | Hiển thị danh sách hoạt động số đếm |
| `CountingGameActivity` | Trò chơi học số đếm |
| `CountingFruitGameActivity` | Trò chơi đếm đồ vật |
| `NumberMatchGameActivity` | Trò chơi ghép số với số lượng |
| `CreateClassActivity` | Tạo lớp và sinh mã tham gia |
| `ClassManagementActivity` | Quản lý danh sách lớp |
| `ClassDetailActivity` | Xem học sinh và bài tập trong lớp |
| `JoinClassActivity` | Phụ huynh cho trẻ tham gia lớp bằng mã lớp |
| `AssignmentManagementActivity`, `CreateAssignmentActivity`, `AssignmentDetailActivity`, `ChildAssignmentActivity` | Quản lý, tạo, xem chi tiết và làm bài tập |
| `TeacherHomeActivity`, `TeacherProfileActivity`, `EditTeacherProfileActivity` | Trang chính và hồ sơ giáo viên |
| `TeacherNotificationActivity`, `NotificationSettingsActivity` | Thông báo và cài đặt thông báo giáo viên |
| `FeedbackListActivity`, `FeedbackChatActivity` | Danh sách phản hồi và trao đổi với phụ huynh |
| `ClassRepository` | Tạo lớp, tìm lớp, quản lý thành viên |
| `AssignmentRepository` | Tạo bài tập và cập nhật bài nộp |
| `ContentRepository`, `ActivityAttemptRepository` | Tải nội dung số đếm và lưu kết quả |

## 3. Code đáp ứng chức năng

### 3.1. Lớp/hàm liên quan

| File | Lớp/hàm | Giải thích |
|---|---|---|
| `child/CountingListActivity.java` | Luồng danh sách số đếm | Hiển thị hoạt động học số đếm |
| `child/CountingGameActivity.java` | Logic số đếm | Kiểm tra đáp án và lưu kết quả |
| `child/CountingFruitGameActivity.java` | Logic đếm đồ vật | Đếm số lượng vật thể |
| `child/NumberMatchGameActivity.java` | Logic ghép số | Ghép số với số lượng tương ứng |
| `data/repository/ClassRepository.java` | `createClass()` | Tạo lớp học |
| `data/repository/ClassRepository.java` | `getClassByJoinCode()` | Tìm lớp theo mã tham gia |
| `data/repository/ClassRepository.java` | `addMember()` | Thêm trẻ vào lớp |
| `data/repository/ClassRepository.java` | `getMembersOfClass()` | Tải danh sách thành viên lớp |
| `data/repository/AssignmentRepository.java` | `createAssignment()` | Tạo bài tập |
| `data/repository/AssignmentRepository.java` | `getAssignmentsByClass()` | Tải bài tập của lớp |
| `data/repository/AssignmentRepository.java` | `submitAssignment()` | Cập nhật bài nộp |
| `teacher/TeacherNotificationActivity.java` | Luồng thông báo | Hiển thị thông báo liên quan đến bài tập, tiến độ, huy hiệu |
| `teacher/FeedbackListActivity.java`, `teacher/FeedbackChatActivity.java` | Luồng phản hồi | Giáo viên xem và trao đổi phản hồi với phụ huynh |
| `teacher/TeacherProfileActivity.java`, `teacher/EditTeacherProfileActivity.java` | Luồng hồ sơ giáo viên | Xem và chỉnh sửa thông tin giáo viên |

### 3.2. Bảng/collection trong CSDL

| Collection | Mục đích |
|---|---|
| `content_catalog` | Danh mục nội dung số đếm |
| `content_catalog/{contentId}/levels` | Level số đếm |
| `child_profiles/{childId}/activity_attempts` | Kết quả hoạt động số đếm |
| `classes` | Lớp học |
| `class_members` | Thành viên lớp |
| `assignments` | Bài tập giáo viên giao |
| `assignment_submissions` | Bài nộp của trẻ |
| `leaderboard_snapshots` | Dữ liệu xếp hạng phục vụ lớp học |
| `feedback_notes` | Phản hồi giữa giáo viên và phụ huynh |
| `child_badges` | Dữ liệu huy hiệu dùng cho thông báo |

### 3.3. API gọi ngoài

| API | File sử dụng | Mục đích |
|---|---|---|
| Cloud Firestore | `ContentRepository`, `ActivityAttemptRepository`, `ClassRepository`, `AssignmentRepository`, `FeedbackRepository` | Lưu nội dung số đếm, lớp học, thành viên lớp, bài tập, bài nộp, thông báo và phản hồi |
| Firebase Authentication | Activity giáo viên/phụ huynh liên quan | Xác định giáo viên hoặc phụ huynh hiện tại |

### 3.4. File code liên quan

| Nhóm | File |
|---|---|
| Học số đếm | `child/CountingListActivity.java`, `child/CountingGameActivity.java`, `child/CountingFruitGameActivity.java`, `child/NumberMatchGameActivity.java` |
| Lớp học | `teacher/CreateClassActivity.java`, `teacher/ClassManagementActivity.java`, `teacher/ClassDetailActivity.java`, `teacher/AddStudentActivity.java`, `parent/JoinClassActivity.java` |
| Bài tập | `teacher/AssignmentManagementActivity.java`, `teacher/CreateAssignmentActivity.java`, `teacher/AssignmentDetailActivity.java`, `child/ChildAssignmentActivity.java` |
| Giáo viên/thông báo/phản hồi không đánh số | `teacher/TeacherHomeActivity.java`, `teacher/TeacherProfileActivity.java`, `teacher/EditTeacherProfileActivity.java`, `teacher/TeacherNotificationActivity.java`, `teacher/NotificationSettingsActivity.java`, `teacher/FeedbackListActivity.java`, `teacher/FeedbackChatActivity.java` |
| Repository/model | `data/repository/ContentRepository.java`, `data/repository/ActivityAttemptRepository.java`, `data/repository/ClassRepository.java`, `data/repository/AssignmentRepository.java`, `data/repository/FeedbackRepository.java`, `data/model/CountingActivity.java`, `data/model/AppClass.java`, `data/model/ClassMember.java`, `data/model/Assignment.java`, `data/model/AssignmentSubmission.java`, `data/model/LeaderboardSnapshot.java`, `data/model/FeedbackNote.java` |

## 4. Hướng dẫn và lưu ý cài đặt, triển khai

- Cần cấu hình Firebase Authentication và Cloud Firestore.
- Firestore rules cần cho phép giáo viên quản lý lớp/bài tập và phụ huynh cho trẻ tham gia lớp.
- Cần chuẩn bị dữ liệu demo cho lớp học, mã tham gia và bài tập.

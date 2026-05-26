# Tài liệu kỹ thuật cá nhân - Kiên

## 1. Danh sách chức năng được phân công

| STT | Nhóm chức năng | Phạm vi |
|---|---|---|
| 1 | Quản lý lớp học | Giáo viên tạo lớp, cấp mã tham gia và xem danh sách học sinh |
| 2 | Đăng ký lớp | Phụ huynh dùng mã lớp để đăng ký lớp cho trẻ |
| 3 | Bài tập và tiến độ lớp | Giáo viên tạo bài tập, xem bài nộp và tiến độ lớp |
| 4 | Thông báo giáo viên | Hiển thị thông báo liên quan đến bài tập, tiến độ và huy hiệu |
| 5 | Module số đếm | Trò chơi học số đếm và lưu kết quả làm bài |

## 2. Kiến trúc chi tiết module

```text
TeacherHomeActivity
  -> ClassManagementActivity / CreateClassActivity / ClassDetailActivity
  -> classes + class_members

CreateAssignmentActivity / AssignmentDetailActivity / AssignmentManagementActivity
  -> assignments + assignment_submissions
  -> child_profiles for child display data

JoinClassActivity
  -> classes by joinCode
  -> class_members + child_profiles class fields

CountingListActivity / CountingGameActivity / CountingFruitGameActivity / NumberMatchGameActivity
  -> activity_attempts or assignment_submissions
```

| Thành phần | Vai trò |
|---|---|
| `ClassRepository` | Tạo lớp, tải danh sách lớp và quản lý thành viên lớp |
| `AssignmentRepository` | Tạo bài tập và cập nhật bài nộp |
| `TeacherHomeActivity` | Màn hình chính của giáo viên |
| `ClassManagementActivity` | Quản lý danh sách lớp |
| `CreateClassActivity` | Tạo lớp và sinh mã tham gia |
| `ClassDetailActivity` | Hiển thị học sinh và bài tập trong lớp |
| `CreateAssignmentActivity` | Tạo bài tập cho lớp |
| `AssignmentDetailActivity` | Hiển thị trạng thái bài nộp |
| `TeacherNotificationActivity` | Hiển thị thông báo cho giáo viên |
| `CountingGameActivity` | Trò chơi học số đếm |

## 3. Code đáp ứng chức năng

### 3.1. Lớp/hàm liên quan

| File | Lớp/hàm | Giải thích |
|---|---|---|
| `data/repository/ClassRepository.java` | `createClass()` | Tạo document lớp học trên Firestore |
| `data/repository/ClassRepository.java` | `getClassByJoinCode()` | Tìm lớp theo mã tham gia |
| `data/repository/ClassRepository.java` | `addMember()` | Thêm trẻ vào lớp |
| `data/repository/ClassRepository.java` | `getMembersOfClass()` | Tải danh sách thành viên lớp |
| `data/repository/AssignmentRepository.java` | `createAssignment()` | Tạo bài tập cho lớp |
| `data/repository/AssignmentRepository.java` | `getAssignmentsByClass()` | Tải danh sách bài tập của lớp |
| `data/repository/AssignmentRepository.java` | `getSubmission()` | Tải bài nộp của trẻ |
| `data/repository/AssignmentRepository.java` | `submitAssignment()` | Cập nhật điểm và trạng thái bài nộp |
| `teacher/CreateClassActivity.java` | Luồng tạo lớp | Kiểm tra form và lưu lớp |
| `teacher/CreateAssignmentActivity.java` | Luồng tạo bài tập | Lưu bài tập lên Firestore |
| `parent/JoinClassActivity.java` | Luồng tham gia lớp | Kiểm tra mã lớp và tạo thành viên lớp |
| `child/CountingGameActivity.java` | Logic trò chơi số đếm | Kiểm tra đáp án và lưu kết quả |
| `child/CountingFruitGameActivity.java` | Logic đếm đồ vật | Đếm số lượng và cập nhật bài nộp khi cần |
| `child/NumberMatchGameActivity.java` | Logic nối số lượng | Ghép số với số lượng đồ vật tương ứng |

### 3.2. Bảng/collection trong CSDL

| Collection | Mục đích |
|---|---|
| `classes` | Lưu dữ liệu lớp, mã giáo viên và mã tham gia |
| `class_members` | Lưu danh sách trẻ đã tham gia lớp |
| `assignments` | Lưu bài tập do giáo viên tạo |
| `assignment_submissions` | Lưu trạng thái bài nộp và điểm số của trẻ |
| `child_profiles` | Tải hồ sơ trẻ cho màn hình lớp và bài tập |
| `child_profiles/{childId}/activity_attempts` | Lưu kết quả hoạt động số đếm |
| `feedback_notes` | Lưu phiên phản hồi giữa giáo viên và phụ huynh |
| `child_badges` | Đọc dữ liệu thông báo liên quan đến huy hiệu |

### 3.3. API gọi ngoài

| API | File sử dụng | Mục đích |
|---|---|---|
| Cloud Firestore | `ClassRepository`, `AssignmentRepository`, activity giáo viên/phụ huynh | Lưu lớp học, thành viên, bài tập và bài nộp |
| Firebase Authentication | Các activity giáo viên | Xác định tài khoản giáo viên hiện tại |

### 3.4. File code liên quan

| Nhóm | File |
|---|---|
| Lớp học | `teacher/ClassManagementActivity.java`, `teacher/CreateClassActivity.java`, `teacher/ClassDetailActivity.java`, `teacher/AddStudentActivity.java`, `parent/JoinClassActivity.java` |
| Bài tập | `teacher/AssignmentManagementActivity.java`, `teacher/CreateAssignmentActivity.java`, `teacher/AssignmentDetailActivity.java`, `child/ChildAssignmentActivity.java` |
| Giáo viên | `teacher/TeacherHomeActivity.java`, `teacher/TeacherProfileActivity.java`, `teacher/EditTeacherProfileActivity.java` |
| Thông báo/phản hồi | `teacher/TeacherNotificationActivity.java`, `teacher/NotificationSettingsActivity.java`, `teacher/FeedbackListActivity.java`, `teacher/FeedbackChatActivity.java` |
| Số đếm | `child/CountingListActivity.java`, `child/CountingGameActivity.java`, `child/CountingFruitGameActivity.java`, `child/NumberMatchGameActivity.java` |
| Repository/model | `data/repository/ClassRepository.java`, `data/repository/AssignmentRepository.java`, `data/model/AppClass.java`, `data/model/ClassMember.java`, `data/model/Assignment.java`, `data/model/AssignmentSubmission.java`, `data/model/CountingActivity.java` |

## 4. Hướng dẫn và lưu ý cài đặt, triển khai

- A teacher account must exist in `accounts`.
- Required collections: `classes`, `class_members`, `assignments`, `assignment_submissions`.
- To test class enrollment, create a class first and use its join code from a parent account.
- To test assignments, at least one class and one child enrolled in that class are required.
- Firestore rules cần cho phép giáo viên quản lý dữ liệu lớp/bài tập và phụ huynh đăng ký lớp cho trẻ.
- Build from `kid_app` with `./gradlew :app:assembleDebug`.
- Relevant classes and external API call sites include Vietnamese no-accent comments using `// Chuc nang: ...`.

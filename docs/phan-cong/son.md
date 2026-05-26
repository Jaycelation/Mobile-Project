# Tài liệu kỹ thuật cá nhân - Nguyễn Thanh Sơn

## 1. Danh sách chức năng được phân công

| Mục | Chức năng | Nhiệm vụ cá nhân |
|---|---|---|
| 2.1.3 | Hoạt động học màu sắc | Thiết kế màn hình tương tác kéo thả và xử lý hiển thị hình ảnh |
| 2.1.6 | Quản lý tài khoản, hồ sơ và cài đặt phụ huynh | Xây dựng hệ thống xác thực người dùng và quản lý cơ sở dữ liệu hồ sơ trẻ |

### Chức năng bổ sung không đánh số

| Chức năng/code bổ sung | File/Class liên quan | Ghi chú |
|---|---|---|
| Màn hình khởi động/điều hướng, quản trị và lớp dùng chung | `SplashActivity`, `WelcomeActivity`, `MainActivity`, `AdminHomeActivity`, `UserManagementActivity`, `ContentManagementActivity`, `SystemReportActivity`, `BaseActivity`, `AppConstants`, `KidLearnApp`, `FirestoreHelper` | Có code trong dự án nhưng không gán thêm chỉ mục mới |

## 2. Kiến trúc chi tiết module

```text
ColorListActivity
  -> ColorGameActivity / ColorMatchGameActivity
  -> ContentRepository / ActivityAttemptRepository

SignInActivity / SignUpActivity / ForgotPasswordActivity
  -> AuthService
  -> Firebase Authentication / accounts

ParentHomeActivity / AddChildActivity / EditChildActivity / ChildSettingsActivity
  -> ChildProfileService / ChildProfileRepository
  -> child_profiles / parent_child_links / settings

SplashActivity / WelcomeActivity / MainActivity
  -> điều hướng khởi động ứng dụng

AdminHomeActivity / UserManagementActivity / ContentManagementActivity / SystemReportActivity
  -> AccountRepository / ContentRepository / Firestore
```

| Thành phần | Vai trò |
|---|---|
| `ColorListActivity` | Hiển thị danh sách hoạt động màu sắc |
| `ColorGameActivity` | Màn học màu sắc dạng tương tác |
| `ColorMatchGameActivity` | Màn ghép màu/kéo thả |
| `SignInActivity`, `SignUpActivity`, `ForgotPasswordActivity` | Đăng nhập, đăng ký, đặt lại mật khẩu |
| `ParentHomeActivity`, `AddChildActivity`, `EditChildActivity`, `ChildSettingsActivity`, `ChildProfileActivity` | Quản lý hồ sơ và cài đặt trẻ |
| `SplashActivity`, `WelcomeActivity`, `MainActivity` | Khởi động và điều hướng ứng dụng |
| `AdminHomeActivity`, `UserManagementActivity`, `ContentManagementActivity`, `SystemReportActivity` | Quản trị người dùng, nội dung và báo cáo |
| `BaseActivity`, `AppConstants`, `KidLearnApp`, `FirestoreHelper` | Thành phần dùng chung và cấu hình nền |
| `AuthService` | Đóng gói Firebase Authentication |
| `AccountRepository` | Đọc/ghi tài khoản |
| `ChildProfileService`, `ChildProfileRepository` | Tạo hồ sơ trẻ, liên kết phụ huynh và cài đặt |
| `ContentRepository`, `ActivityAttemptRepository` | Tải nội dung màu sắc và lưu kết quả |

## 3. Code đáp ứng chức năng

### 3.1. Lớp/hàm liên quan

| File | Lớp/hàm | Giải thích |
|---|---|---|
| `child/ColorListActivity.java` | Luồng danh sách màu sắc | Hiển thị hoạt động học màu sắc |
| `child/ColorGameActivity.java` | Logic học màu | Xử lý chọn màu và lưu kết quả |
| `child/ColorMatchGameActivity.java` | Logic ghép màu | Xử lý kéo thả/ghép màu |
| `auth/AuthService.java` | `signIn()` | Đăng nhập bằng Firebase Authentication |
| `auth/AuthService.java` | `signUp()` | Tạo tài khoản Firebase và document `accounts` |
| `auth/AuthService.java` | `sendPasswordResetEmail()` | Gửi email đặt lại mật khẩu |
| `auth/AuthService.java` | `getCurrentUserAccount()` | Tải tài khoản hiện tại và vai trò |
| `parent/ChildProfileService.java` | `createChildProfile()` | Tạo hồ sơ trẻ, liên kết phụ huynh và thống kê ban đầu |
| `parent/ChildProfileService.java` | `saveChildSettings()` | Lưu cài đặt học tập của trẻ |
| `data/repository/ChildProfileRepository.java` | `createChildProfile()` | Ghi hồ sơ trẻ vào Firestore |
| `data/repository/ChildProfileRepository.java` | `createParentChildLink()` | Liên kết tài khoản phụ huynh với hồ sơ trẻ |
| `data/repository/ContentRepository.java` | `getContentByType()` | Tải nội dung màu sắc |
| `data/repository/ActivityAttemptRepository.java` | `completeAttempt()` | Lưu kết quả hoạt động màu sắc |
| `admin/UserManagementActivity.java` | Luồng quản lý người dùng | Quản trị danh sách tài khoản |
| `admin/ContentManagementActivity.java` | Luồng quản lý nội dung | Quản trị danh mục nội dung học tập |
| `admin/SystemReportActivity.java` | Luồng báo cáo | Đọc dữ liệu tổng quan hệ thống |
| `SplashActivity.java`, `WelcomeActivity.java`, `MainActivity.java` | Luồng điều hướng | Khởi động và chuyển màn hình chính |

### 3.2. Bảng/collection trong CSDL

| Collection | Mục đích |
|---|---|
| `accounts` | Tài khoản, email, họ tên và vai trò |
| `child_profiles` | Hồ sơ trẻ |
| `parent_child_links` | Liên kết phụ huynh - trẻ |
| `child_profiles/{childId}/settings` | Cài đặt học tập |
| `content_catalog` | Nội dung học màu sắc |
| `content_catalog/{contentId}/levels` | Level màu sắc |
| `child_profiles/{childId}/activity_attempts` | Kết quả hoạt động màu sắc |
| `child_stats` | Thống kê học tập ban đầu |
| `content_catalog/{contentId}/levels` | Level nội dung học tập |

### 3.3. API gọi ngoài

| API | File sử dụng | Mục đích |
|---|---|---|
| Firebase Authentication | `AuthService`, `SignInActivity`, `ForgotPasswordActivity` | Đăng nhập, đăng ký, đặt lại mật khẩu |
| Cloud Firestore | `AccountRepository`, `ChildProfileRepository`, `ContentRepository`, `ActivityAttemptRepository`, các activity quản trị | Lưu tài khoản, hồ sơ trẻ, cài đặt, nội dung màu sắc, kết quả và báo cáo quản trị |

### 3.4. File code liên quan

| Nhóm | File |
|---|---|
| Học màu sắc | `child/ColorListActivity.java`, `child/ColorGameActivity.java`, `child/ColorMatchGameActivity.java` |
| Xác thực | `auth/AuthService.java`, `auth/SignInActivity.java`, `auth/SignUpActivity.java`, `auth/ForgotPasswordActivity.java` |
| Hồ sơ/cài đặt phụ huynh | `parent/ParentHomeActivity.java`, `parent/AddChildActivity.java`, `parent/EditChildActivity.java`, `parent/ChildSettingsActivity.java`, `parent/ChildProfileService.java`, `child/ChildProfileActivity.java` |
| Quản trị/nền tảng không đánh số | `SplashActivity.java`, `WelcomeActivity.java`, `MainActivity.java`, `admin/AdminHomeActivity.java`, `admin/UserManagementActivity.java`, `admin/ContentManagementActivity.java`, `admin/SystemReportActivity.java`, `common/BaseActivity.java`, `common/AppConstants.java`, `KidLearnApp.java`, `data/FirestoreHelper.java` |
| Repository/model | `data/repository/AccountRepository.java`, `data/repository/ChildProfileRepository.java`, `data/repository/ContentRepository.java`, `data/repository/ActivityAttemptRepository.java`, `data/model/Account.java`, `data/model/ParentChildLink.java`, `data/model/ChildProfile.java`, `data/model/ChildSettings.java`, `data/model/ContentCatalog.java`, `data/model/ColorActivity.java`, `data/model/ContentLevel.java`, `data/model/ActivityAttempt.java` |

## 4. Hướng dẫn và lưu ý cài đặt, triển khai

- Bật Firebase Authentication với phương thức Email/Password.
- Chuẩn bị các collection Firestore: `accounts`, `child_profiles`, `parent_child_links`, `child_stats`, `content_catalog`.
- Firestore rules cần phân quyền phụ huynh chỉ quản lý đúng hồ sơ trẻ liên kết với tài khoản của mình.

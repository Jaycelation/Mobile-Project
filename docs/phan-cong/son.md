# Tài liệu kỹ thuật cá nhân - Sơn

## 1. Danh sách chức năng được phân công

| STT | Nhóm chức năng | Phạm vi |
|---|---|---|
| 1 | Xác thực và phân quyền | Đăng nhập, đăng ký, đặt lại mật khẩu, đăng xuất, điều hướng theo vai trò |
| 2 | Quản lý hồ sơ trẻ | Tạo, cập nhật, chọn hồ sơ trẻ và quản lý cài đặt học tập |
| 3 | Quản trị hệ thống | Quản lý tài khoản, nội dung học tập và báo cáo tổng quan |
| 4 | Nền tảng dùng chung | Splash, màn hình chào, base activity, hằng số và tài nguyên dùng chung |

## 2. Kiến trúc chi tiết module

```text
SignInActivity / SignUpActivity / ForgotPasswordActivity
  -> AuthService
  -> FirebaseAuth + accounts
  -> SplashActivity điều hướng theo vai trò

ParentHomeActivity / AddChildActivity / EditChildActivity / ChildSettingsActivity
  -> ChildProfileService
  -> ChildProfileRepository
  -> child_profiles + parent_child_links + child_stats + settings

AdminHomeActivity / UserManagementActivity / ContentManagementActivity / SystemReportActivity
  -> AccountRepository / ContentRepository
  -> accounts + content_catalog + report data
```

| Thành phần | Vai trò |
|---|---|
| `AuthService` | Đóng gói Firebase Authentication và tra cứu tài khoản |
| `AccountRepository` | Đọc/ghi dữ liệu trong collection `accounts` |
| `ChildProfileService` | Điều phối tạo hồ sơ trẻ và lưu cài đặt học tập |
| `ChildProfileRepository` | Quản lý hồ sơ trẻ, cài đặt, thống kê và liên kết phụ huynh-trẻ |
| `BaseActivity` | Cung cấp tiện ích giao diện dùng chung như loading, toast và điều hướng |
| `AppConstants` | Lưu tên collection, intent key và preference key dùng chung |
| Các activity `admin` | Màn hình quản trị, quản lý tài khoản/nội dung và báo cáo |

## 3. Code đáp ứng chức năng

### 3.1. Lớp/hàm liên quan

| File | Lớp/hàm | Giải thích |
|---|---|---|
| `auth/AuthService.java` | `signIn()` | Đăng nhập bằng Firebase Authentication |
| `auth/AuthService.java` | `signUp()` | Tạo tài khoản Firebase và document trong `accounts` |
| `auth/AuthService.java` | `sendPasswordResetEmail()` | Gửi email đặt lại mật khẩu |
| `auth/AuthService.java` | `getCurrentUserAccount()` | Tải thông tin tài khoản hiện tại và vai trò |
| `auth/SignInActivity.java` | `attemptSignIn()` | Kiểm tra form đăng nhập và gọi xử lý đăng nhập |
| `auth/SignInActivity.java` | `loadUserAndNavigate()` | Điều hướng màn hình theo vai trò tài khoản |
| `parent/ChildProfileService.java` | `createChildProfile()` | Tạo hồ sơ trẻ, liên kết phụ huynh và thống kê ban đầu |
| `parent/ChildProfileService.java` | `saveChildSettings()` | Lưu cài đặt học tập của trẻ |
| `data/repository/ChildProfileRepository.java` | `createChildProfile()` | Ghi hồ sơ trẻ vào Firestore |
| `data/repository/ChildProfileRepository.java` | `createParentChildLink()` | Liên kết tài khoản phụ huynh với hồ sơ trẻ |
| `data/repository/ChildProfileRepository.java` | `initChildStats()` | Khởi tạo thống kê học tập của trẻ |
| `admin/ContentManagementActivity.java` | `loadContent()` | Tải danh sách nội dung học tập |
| `admin/SystemReportActivity.java` | `loadReport()` | Tải dữ liệu báo cáo tổng quan hệ thống |

### 3.2. Bảng/collection trong CSDL

| Collection | Mục đích |
|---|---|
| `accounts` | Lưu hồ sơ tài khoản, email, họ tên và vai trò |
| `child_profiles` | Lưu dữ liệu hồ sơ trẻ |
| `parent_child_links` | Lưu liên kết giữa phụ huynh và trẻ |
| `child_profiles/{childId}/settings` | Lưu cài đặt học tập |
| `child_stats` | Lưu dữ liệu tiến độ ban đầu và tiến độ tích lũy |
| `content_catalog` | Lưu nội dung học tập do quản trị viên quản lý |

### 3.3. API gọi ngoài

| API | File sử dụng | Mục đích |
|---|---|---|
| Firebase Authentication | `AuthService`, `SignInActivity`, `ForgotPasswordActivity` | Đăng nhập, đăng ký, đặt lại mật khẩu |
| Cloud Firestore | `AccountRepository`, `ChildProfileRepository`, `ContentRepository` | Lưu tài khoản, hồ sơ và nội dung học tập |

### 3.4. File code liên quan

| Nhóm | File |
|---|---|
| Xác thực | `auth/AuthService.java`, `auth/SignInActivity.java`, `auth/SignUpActivity.java`, `auth/ForgotPasswordActivity.java` |
| Điều hướng/dùng chung | `SplashActivity.java`, `WelcomeActivity.java`, `MainActivity.java`, `KidLearnApp.java`, `common/BaseActivity.java`, `common/AppConstants.java` |
| Hồ sơ phụ huynh/trẻ | `parent/ParentHomeActivity.java`, `parent/AddChildActivity.java`, `parent/EditChildActivity.java`, `parent/ChildSettingsActivity.java`, `parent/ChildProfileService.java` |
| Quản trị | `admin/AdminHomeActivity.java`, `admin/UserManagementActivity.java`, `admin/ContentManagementActivity.java`, `admin/SystemReportActivity.java` |
| Repository | `data/repository/AccountRepository.java`, `data/repository/ChildProfileRepository.java`, `data/FirestoreHelper.java` |

## 4. Hướng dẫn và lưu ý cài đặt, triển khai

- Bật Firebase Authentication với phương thức Email/Password.
- Chuẩn bị các collection Firestore: `accounts`, `child_profiles`, `parent_child_links`, `child_stats`, `content_catalog`.
- Place `google-services.json` in `kid_app/app/` for local builds only.
- Do not commit `google-services.json`, `local.properties`, logs or report drafts.
- Build from `kid_app` with `./gradlew :app:assembleDebug`.
- Use an ASCII-only path on Windows to avoid Android Gradle Plugin path issues.
- Relevant classes and external API call sites include Vietnamese no-accent comments using `// Chuc nang: ...`.

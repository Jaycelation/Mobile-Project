# Tài liệu kỹ thuật cá nhân - Tài

## 1. Danh sách chức năng được phân công

| STT | Nhóm chức năng | Phạm vi |
|---|---|---|
| 1 | Trò chơi tư duy logic và phản xạ | Thiết kế giao diện trò chơi và lập trình logic xử lý |
| 2 | Trò chơi ghép hình và nhận biết | Ghép tranh, ghép bóng, nhận biết hình dạng/đồ vật/âm thanh |
| 3 | Tiến độ, huy hiệu và bảng xếp hạng | Lưu thành tích, hiển thị tiến độ, huy hiệu và bảng xếp hạng |

## 2. Kiến trúc chi tiết module

```text
GameListActivity / ChildHomeActivity
  -> PatternGameActivity / FastEyeGameActivity / PuzzleGameActivity / ...
  -> ActivityAttemptRepository hoặc gọi trực tiếp Firestore
  -> child_profiles/{childId}/activity_attempts
  -> ChildProfileRepository.addPoints()
  -> child_stats + child_badges

ChildProgressActivity / BadgeCollectionActivity / LeaderboardActivity
  -> child_stats + child_badges + class_members + leaderboard_snapshots
  -> progress, badge and ranking UI
```

| Thành phần | Vai trò |
|---|---|
| `GameListActivity` | Hiển thị danh sách trò chơi giáo dục |
| `PatternGameActivity` | Trò chơi nhận biết quy luật |
| `FastEyeGameActivity` | Trò chơi phản xạ nhanh |
| `PuzzleGameActivity` | Trò chơi ghép tranh |
| `ShadowMatchGameActivity` | Trò chơi ghép bóng |
| `AnimalGameActivity`, `AnimalSoundGameActivity` | Nhận biết hình ảnh và âm thanh |
| `ChildProgressActivity` | Hiển thị tiến độ học tập của trẻ |
| `BadgeCollectionActivity` | Hiển thị huy hiệu đã đạt |
| `LeaderboardActivity` | Hiển thị bảng xếp hạng theo lớp |
| `ActivityAttemptRepository` | Lưu lượt chơi và đáp án |
| `BadgeRepository` | Lưu và tải dữ liệu huy hiệu |
| `ChildProfileRepository` | Cập nhật điểm, chuỗi học liên tiếp và huy hiệu đã nhận |

## 3. Code đáp ứng chức năng

### 3.1. Lớp/hàm liên quan

| File | Lớp/hàm | Giải thích |
|---|---|---|
| `data/repository/ActivityAttemptRepository.java` | `startAttempt()` | Tạo lượt chơi/lượt học mới |
| `data/repository/ActivityAttemptRepository.java` | `completeAttempt()` | Lưu điểm, trạng thái và thời gian hoàn thành |
| `data/repository/ActivityAttemptRepository.java` | `saveAnswer()` | Lưu dữ liệu đáp án chi tiết |
| `data/repository/BadgeRepository.java` | `awardBadge()` | Trao huy hiệu cho trẻ |
| `data/repository/BadgeRepository.java` | `getBadgesOfChild()` | Tải danh sách huy hiệu đã đạt |
| `data/repository/ChildProfileRepository.java` | `addPoints()` | Cộng điểm và kiểm tra mốc huy hiệu |
| `data/repository/ChildProfileRepository.java` | `completeLesson()` | Đánh dấu bài học đã hoàn thành |
| `child/PatternGameActivity.java` | Logic trò chơi | Kiểm tra đáp án còn thiếu trong quy luật |
| `child/FastEyeGameActivity.java` | Logic trò chơi | Kiểm tra thao tác chạm mục tiêu phản xạ |
| `child/PuzzleGameActivity.java` | Logic trò chơi | Kiểm tra vị trí đặt mảnh ghép |
| `child/ChildProgressActivity.java` | Tải tiến độ | Tải dữ liệu `child_stats` |
| `child/LeaderboardActivity.java` | Tải bảng xếp hạng | Tải thành viên lớp và điểm của trẻ |

### 3.2. Bảng/collection trong CSDL

| Collection | Mục đích |
|---|---|
| `child_profiles/{childId}/activity_attempts` | Lưu lịch sử chơi, điểm, thời gian và trạng thái hoàn thành |
| `child_profiles/{childId}/activity_attempts/{attemptId}/answers` | Lưu đáp án chi tiết |
| `child_stats` | Lưu tổng điểm, chuỗi học liên tiếp và số bài đã hoàn thành |
| `badges` | Danh mục huy hiệu |
| `child_badges` | Huy hiệu mỗi trẻ đã đạt |
| `class_members` | Tải danh sách trẻ trong lớp cho bảng xếp hạng |
| `leaderboard_snapshots` | Lưu/tải dữ liệu bảng xếp hạng |
| `assignment_submissions` | Lưu kết quả trò chơi khi mở từ bài tập |

### 3.3. API gọi ngoài

| API | File sử dụng | Mục đích |
|---|---|---|
| Cloud Firestore | Các activity trò chơi, repository | Lưu kết quả trò chơi, thống kê, huy hiệu và bảng xếp hạng |
| Firebase Authentication | Một số màn hình liên quan | Xác định người dùng hiện tại khi cần |

### 3.4. File code liên quan

| Nhóm | File |
|---|---|
| Danh sách/trang chính trò chơi | `child/GameListActivity.java`, `child/ChildHomeActivity.java`, `child/ChildProfileActivity.java` |
| Trò chơi logic/phản xạ | `child/PatternGameActivity.java`, `child/FastEyeGameActivity.java`, `child/PuzzleGameActivity.java` |
| Trò chơi ghép/nhận biết | `child/AlphabetMatchGameActivity.java`, `child/ShapeGameActivity.java`, `child/ShadowMatchGameActivity.java`, `child/ObjectGameActivity.java`, `child/AnimalGameActivity.java`, `child/AnimalSoundGameActivity.java`, `child/FruitMatchGameActivity.java` |
| Tiến độ/huy hiệu/xếp hạng | `child/ChildProgressActivity.java`, `child/BadgeCollectionActivity.java`, `child/LeaderboardActivity.java` |
| Repository/model | `data/repository/ActivityAttemptRepository.java`, `data/repository/BadgeRepository.java`, `data/repository/ChildProfileRepository.java`, `data/model/ActivityAttempt.java`, `data/model/AttemptAnswer.java`, `data/model/Game.java`, `data/model/Badge.java`, `data/model/ChildBadge.java`, `data/model/ChildStats.java`, `data/model/LeaderboardSnapshot.java` |

## 4. Hướng dẫn và lưu ý cài đặt, triển khai

- A selected child profile is required before launching games.
- Required collections: `child_stats`, `badges`, `child_badges`, `activity_attempts`.
- Leaderboard testing requires children enrolled in a class through `class_members`.
- Assignment-based game testing requires an `assignmentId` to create/update `assignment_submissions`.
- Image/audio resources used by games must exist in `res/drawable` and `res/raw`.
- Build from `kid_app` with `./gradlew :app:assembleDebug`.
- Relevant classes and external API call sites include Vietnamese no-accent comments using `// Chuc nang: ...`.

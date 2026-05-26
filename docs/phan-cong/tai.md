# Tài liệu kỹ thuật cá nhân - Đặng Đức Tài

## 1. Danh sách chức năng được phân công

| Mục | Chức năng | Nhiệm vụ cá nhân |
|---|---|---|
| 2.1.1 | Trò chơi giáo dục tương tác | Thiết kế giao diện game và lập trình thuật toán logic trò chơi |
| 2.1.8 | Hệ thống phản hồi và khen thưởng | Xây dựng hiệu ứng chúc mừng, huy hiệu và lưu trữ thành tích học tập |

## 2. Kiến trúc chi tiết module

```text
GameListActivity
  -> PatternGameActivity / FastEyeGameActivity / PuzzleGameActivity
  -> ActivityAttemptRepository
  -> child_profiles/{childId}/activity_attempts

BadgeCollectionActivity / ChildProgressActivity / LeaderboardActivity
  -> BadgeRepository / FeedbackRepository
  -> badges / child_badges / child_stats / leaderboard_snapshots

ParentFeedbackActivity / ParentFeedbackListActivity
  -> FeedbackRepository
  -> feedback_notes
```

| Thành phần | Vai trò |
|---|---|
| `GameListActivity` | Hiển thị danh sách trò chơi giáo dục |
| `PatternGameActivity` | Trò chơi tìm quy luật |
| `FastEyeGameActivity` | Trò chơi phản xạ nhanh |
| `PuzzleGameActivity` | Trò chơi ghép tranh |
| `BadgeCollectionActivity` | Hiển thị huy hiệu trẻ đã đạt |
| `ChildProgressActivity` | Hiển thị điểm và tiến độ học tập |
| `LeaderboardActivity` | Hiển thị bảng xếp hạng |
| `ParentFeedbackActivity`, `ParentFeedbackListActivity` | Hiển thị phản hồi/khen thưởng |
| `ActivityAttemptRepository` | Lưu lượt chơi, đáp án và kết quả |
| `BadgeRepository` | Lưu/tải huy hiệu |
| `FeedbackRepository` | Lưu phản hồi và dữ liệu bảng xếp hạng |
| `ContentRepository` | Tải nội dung game/level |

## 3. Code đáp ứng chức năng

### 3.1. Lớp/hàm liên quan

| File | Lớp/hàm | Giải thích |
|---|---|---|
| `child/GameListActivity.java` | Luồng danh sách game | Hiển thị các trò chơi giáo dục cho trẻ |
| `child/PatternGameActivity.java` | Logic trò chơi | Kiểm tra đáp án còn thiếu trong quy luật |
| `child/FastEyeGameActivity.java` | Logic trò chơi | Kiểm tra thao tác chạm mục tiêu phản xạ |
| `child/PuzzleGameActivity.java` | Logic trò chơi | Kiểm tra vị trí đặt mảnh ghép |
| `data/repository/ActivityAttemptRepository.java` | `startAttempt()` | Tạo lượt chơi/lượt học mới |
| `data/repository/ActivityAttemptRepository.java` | `completeAttempt()` | Lưu điểm, trạng thái và thời gian hoàn thành |
| `data/repository/ActivityAttemptRepository.java` | `saveAnswer()` | Lưu đáp án chi tiết |
| `data/repository/BadgeRepository.java` | `awardBadge()` | Trao huy hiệu cho trẻ |
| `data/repository/BadgeRepository.java` | `getChildBadges()` | Tải huy hiệu trẻ đã đạt |
| `data/repository/FeedbackRepository.java` | `createFeedback()` | Tạo phản hồi/khen thưởng |
| `data/repository/FeedbackRepository.java` | `getFeedbackForChild()` | Tải phản hồi của trẻ |
| `data/repository/FeedbackRepository.java` | `saveSnapshot()` | Lưu dữ liệu bảng xếp hạng |
| `child/ChildProgressActivity.java` | Tải tiến độ | Đọc `child_stats` |
| `child/LeaderboardActivity.java` | Tải bảng xếp hạng | Đọc thành viên lớp và điểm của trẻ |

### 3.2. Bảng/collection trong CSDL

| Collection | Mục đích |
|---|---|
| `content_catalog` | Danh mục nội dung game |
| `content_catalog/{contentId}/levels` | Level của game |
| `child_profiles/{childId}/activity_attempts` | Lịch sử chơi game |
| `child_profiles/{childId}/activity_attempts/{attemptId}/answers` | Đáp án chi tiết |
| `child_stats` | Điểm, chuỗi học liên tiếp và tiến độ |
| `badges` | Danh mục huy hiệu |
| `child_badges` | Huy hiệu trẻ đã nhận |
| `feedback_notes` | Phản hồi/khen thưởng |
| `leaderboard_snapshots` | Dữ liệu bảng xếp hạng |

### 3.3. API gọi ngoài

| API | File sử dụng | Mục đích |
|---|---|---|
| Cloud Firestore | Các activity trò chơi, `ActivityAttemptRepository`, `BadgeRepository`, `FeedbackRepository` | Lưu kết quả, huy hiệu, phản hồi và bảng xếp hạng |

### 3.4. File code liên quan

| Nhóm | File |
|---|---|
| Trò chơi giáo dục | `child/GameListActivity.java`, `child/PatternGameActivity.java`, `child/FastEyeGameActivity.java`, `child/PuzzleGameActivity.java` |
| Phản hồi/khen thưởng | `child/BadgeCollectionActivity.java`, `child/ChildProgressActivity.java`, `child/LeaderboardActivity.java`, `child/ParentFeedbackActivity.java`, `child/ParentFeedbackListActivity.java` |
| Repository/model | `data/repository/ActivityAttemptRepository.java`, `data/repository/BadgeRepository.java`, `data/repository/FeedbackRepository.java`, `data/repository/ContentRepository.java`, `data/model/ActivityAttempt.java`, `data/model/Game.java`, `data/model/Badge.java`, `data/model/ChildBadge.java`, `data/model/FeedbackNote.java`, `data/model/ChildStats.java`, `data/model/LeaderboardSnapshot.java` |

## 4. Hướng dẫn và lưu ý cài đặt, triển khai

- Cần cấu hình Firebase và `google-services.json`.
- Firestore rules cần cho phép trẻ/phụ huynh đọc kết quả, huy hiệu và phản hồi đúng quyền.
- Khi build local, chạy trong thư mục `kid_app`.

# BTL MAD – Ứng dụng giáo dục cho trẻ em (Android Java + Firebase + AI Trợ lý học tập)

**Backend:** Firebase (Auth, Firestore, Storage, Cloud Functions)  
**Mobile:** Android Studio – Java (XML UI, Firebase SDK)  
**AI:** Trợ lý học tập qua API (gọi từ Cloud Functions để không lộ API key)

---

## 1) Actors (vai trò hệ thống)
- **Child (Trẻ em):** chọn hồ sơ, học/chơi, làm quiz, nhận thưởng, hỏi trợ lý.
- **Parent (Phụ huynh):** đăng nhập, tạo/quản lý hồ sơ trẻ, cài đặt giới hạn, xem tiến độ.
- **Teacher (Giáo viên):** đăng nhập, tạo lớp, xem tiến độ lớp, (tuỳ chọn) giao bài.
- **Admin (tuỳ chọn):** quản lý nội dung/duyệt report.

---

## 2) Firestore Collections (Schema đề xuất)

### 2.1. `users/{uid}`
- `role`: `"parent" | "teacher" | "admin"`
- `displayName`, `email`
- `createdAt`

### 2.2. `children/{childId}`
- `parentUid`
- `name`, `age`, `avatarUrl`, `levelSuggested`
- `settings`:
  - `dailyTimeLimitMin`
  - `enableAI`
  - `enableCommunity`
  - `allowedTypes` (vd: ["quiz","color","counting","game"])
- `createdAt`, `updatedAt`

### 2.3. `contents/{contentId}`
- `type`: `"game" | "quiz" | "color" | "counting"`
- `title`, `topic`, `minAge`, `maxAge`, `level`, `description`
- `thumbnailRef`/`thumbnailUrl`, `assets` (Storage paths)
- `createdAt`, `updatedAt`

---

## 3) Quiz

### 3.1. `quizzes/{quizId}`  (Client đọc 1 lần)
**Chỉ chứa dữ liệu public để render UI**
- `contentId` (nếu quiz là 1 content)
- `title`, `topic`, `level`, `minAge`, `maxAge`
- `questions`: **array** (mảng câu hỏi)
  - mỗi phần tử:
    - `questionId`
    - `prompt`
    - `options`: array[string]
    - `explanation` (tuỳ)
    - `order`

### 3.2. `quiz_keys/{quizId}` (Server-only)
**Chỉ Cloud Functions/service account được đọc**
- `answerKey`: map
  - key: `questionId`
  - value: `correctAnswer`

---

## 4) Attempts / Sessions / Achievements

### 4.1. `attempts/{attemptId}`
- `childId`, `quizId`
- `answers`: array map `{questionId, answer, timeSpentSec}`
- `status`: `"doing" | "submitted"`
- `result`: `{correctCount, total, score, feedback}`
- `createdAt`, `submittedAt`

### 4.2. `sessions/{sessionId}`
- `childId`, `contentId`, `type`
- `score`, `durationSec`, `mistakes`, `meta`
- `createdAt`, `completedAt`

### 4.3. `achievements/{childId}/badges/{badgeId}`
- `title`, `earnedAt`, `reason`

---

## 5) Classes / Assignments (Teacher)

### 5.1. `classes/{classId}`
- `teacherUid`, `className`
- `joinCode`
- `studentChildIds`: array[string]
- `createdAt`

### 5.2. `assignments/{assignmentId}`
- `classId`, `contentId`
- `dueDate` (tuỳ), `createdAt`

---

## 6) Cloud Functions (API nghiệp vụ)

### 6.1. `assistantChat(childId, threadId, message, context)`
- Trả: `{threadId, replyText}` (+ flags nếu có)
- Lưu message vào `assistant_messages` (tuỳ)

### 6.2. `gradeAttempt(attemptId)`
- Đọc: `attempts/{attemptId}`, `quizzes/{quizId}` (questions array), `quiz_keys/{quizId}` (answerKey)
- Update: `attempts/{attemptId}.result` + `status="submitted"`
- Trả: `{correctCount, total, score, feedback}`

### 6.3. `startSession(childId, contentId)`
- Tạo `sessions/{sessionId}`
- Trả: `{sessionId, startedAt}`

### 6.4. `completeSession(sessionId, score, durationSec, mistakes, meta)`
- Update session
- Cập nhật `achievements` (server-only)
- Trả: `{awardedBadges: []}` (tuỳ)

### 6.5. `joinClassByCode(joinCode, childId)`
- Add `childId` vào `classes.studentChildIds` (check owner)
- Trả: `{classId, className}`

### 6.6. `getProgressSummary(childId, from, to)` (optional)
- Aggregate từ sessions/attempts/achievements
- Trả: `{totalSessions, avgScore, quizStats, badges...}`

---

# 7) BẢNG PHÂN CÔNG

| API module | Thành viên 1 | Thành viên 2 | Thành viên 3 | Thành viên 4 |
|---|---|---|---|---|
| 1) Auth & User Role API (`Firebase Auth` + `users`) | **Register/login/logout; tạo `users/{uid}` + role; điều hướng UI theo role; rules phần `users`; test accounts** | - | - | - |
| 2) Children & Settings API (`children`) | - | **CRUD `children`; UI chọn hồ sơ trẻ; settings (dailyTimeLimitMin/enableAI/enableCommunity/allowedTypes); rules children; test** | - | - |
| 3) Content Catalog API (`contents` + Storage) | - | - | **Catalog list/filter/detail; load thumbnail/assets từ Storage (dùng Glide/Coil); xử lý luồng âm thanh game (SoundPool/ExoPlayer); seed contents; rules đọc contents** | - |
| 4) Activities UI (Color & Counting + kết quả) | - | - | **Code 2 màn game (Màu sắc, Số đếm): tương tác (kéo-thả/chọn), feedback; ghi kết quả về `sessions` hoặc `activity_results`; test UX** | - |
| 5) Sessions & Rewards API (`sessions` + `achievements`, Functions start/complete) | - | **Cloud Functions `startSession` + `completeSession`; logic tính điểm/thưởng/huy hiệu; update `sessions` + `achievements`; rules sessions/achievements; cung cấp data cho Progress** | - | - |
| 6) Quiz & Attempts API (quiz array + key doc, `attempts`, Function grade) | - | - | - | **Quiz UI; tạo attempt + lưu answers; Function `gradeAttempt`; hiển thị kết quả/giải thích; seed `quizzes` + `quiz_keys`; rules attempts/quiz; test** |
| 7) Class & Assignment API (`classes`, `assignments`, join code) | **Teacher UI tạo lớp + joinCode; Function `joinClassByCode`; CRUD assignments; rules classes/assignments; test join** | - | - | - |
| 8) AI Learning Assistant API (Function `assistantChat`, threads/messages) | - | - | - | **Function `assistantChat` gọi AI API; prompt kid-safe; (tuỳ) lưu thread/messages; Android UI chat; test & log** |
| 9) Progress Dashboard API (tổng hợp sessions/attempts/badges) | - | **Progress UI ngày/tuần; query/aggregate từ `sessions` + `attempts` + `achievements`; (optional) Function `getProgressSummary`; test bằng data thật** | - | - |

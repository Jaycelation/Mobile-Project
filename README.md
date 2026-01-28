# Mobile-Project

## 1) Auth + phân quyền (Parent/Teacher/Child)

* **POST** `/auth/register` (parent/teacher)
* **POST** `/auth/login` → trả `access_token`, `refresh_token`
* **POST** `/auth/refresh`
* **POST** `/auth/logout`
* **GET** `/users/me`
* **PATCH** `/users/me` (tên, avatar, ngôn ngữ)

**Quản lý hồ sơ trẻ (do parent tạo):**

* **POST** `/children` (tạo profile trẻ: tên, tuổi, avatar, cấp lớp)
* **GET** `/children` (danh sách trẻ của phụ huynh)
* **GET** `/children/{childId}`
* **PATCH** `/children/{childId}`
* **DELETE** `/children/{childId}`

---

## 2) Catalog nội dung học (Games / Quizzes / Colors / Counting)

* **GET** `/catalog` (tất cả nội dung, có filter)

  * query: `type=game|quiz|color|counting`, `age=4-6`, `topic=math`, `level=1`
* **GET** `/catalog/{contentId}` (chi tiết 1 nội dung)
* **GET** `/catalog/{contentId}/assets` (ảnh, âm thanh, video; nên trả signed URL)

> Nếu có CMS: admin/teacher tạo nội dung sẽ dùng nhóm API "Content Management" ở cuối.

---

## 3) Game sessions (chơi game & lưu tiến trình)

* **POST** `/sessions`

  * body: `{ childId, contentId, deviceTime, mode }`
* **POST** `/sessions/{sessionId}/events` (log sự kiện theo thời gian thực)

  * ví dụ: `{ type:"MOVE", payload:{...}, at }`, `{ type:"HINT_USED" ... }`
* **POST** `/sessions/{sessionId}/complete`

  * body: `{ score, stars, durationSec, mistakes, rewards }`
* **GET** `/children/{childId}/sessions?contentId=&from=&to=`

---

## 4) Câu đố & trắc nghiệm (Quiz/Attempt)

* **POST** `/quizzes/{quizId}/attempts` (tạo lượt làm bài)
* **POST** `/attempts/{attemptId}/answers` (gửi từng câu)

  * `{ questionId, answer, timeSpentSec }`
* **POST** `/attempts/{attemptId}/submit` → trả kết quả

  * `{ correctCount, total, score, explanations[] }`
* **GET** `/attempts/{attemptId}` (xem lại)
* **GET** `/quizzes/{quizId}/questions` *(tuỳ chọn, nếu không nhúng trong catalog)*

---

## 5) Hoạt động học Màu sắc

* **GET** `/colors` (danh sách màu: tên, mã màu, hình minh hoạ)
* **GET** `/colors/{colorId}`
* **POST** `/children/{childId}/color-activities/{activityId}/submit`

  * ví dụ: chọn màu đúng, tô màu, ghép màu → `{ result, timeSpentSec }`

---

## 6) Hoạt động học Số đếm

* **GET** `/counting/levels` hoặc `/counting/activities`
* **GET** `/counting/activities/{activityId}`
* **POST** `/children/{childId}/counting-activities/{activityId}/submit`

  * `{ answers, result, timeSpentSec }`

---

## 7) Phản hồi tương tác + ghi chú cá nhân

**Phản hồi hệ thống thường làm client-side**, nhưng vẫn cần API để lưu "nhật ký học" & "gợi ý cá nhân hoá":

* **POST** `/children/{childId}/notes` (phụ huynh/giáo viên ghi chú)
* **GET** `/children/{childId}/notes?from=&to=`
* **GET** `/children/{childId}/recommendations` (gợi ý bài phù hợp từ tiến trình)
* **POST** `/children/{childId}/rewards/claim` (nhận huy hiệu/quà)

---

## 8) Tiến trình học tập (Progress/Analytics)

* **GET** `/children/{childId}/progress`

  * trả theo kỹ năng: logic, toán, màu sắc, phản xạ…
* **GET** `/children/{childId}/achievements` (huy hiệu)
* **GET** `/children/{childId}/summary?period=week|month`
* **POST** `/telemetry` *(tuỳ chọn)*: log thống kê ẩn danh (cẩn thận dữ liệu trẻ em)

---

## 9) An toàn + phụ huynh kiểm soát (Parental Controls)

* **PATCH** `/children/{childId}/settings`

  * `{ dailyTimeLimitMin, quietHours, allowedContentTypes, communityEnabled }`
* **POST** `/consents` (lưu đồng ý của phụ huynh cho tính năng cộng đồng/thu thập dữ liệu)
* **GET** `/safety/policy` (điều khoản, hướng dẫn an toàn)
* **POST** `/reports` (báo cáo nội dung xấu / bắt nạt / spam)

---

## 10) Cộng đồng + giáo viên (Feed, lớp học, chia sẻ thành tựu)

**Cộng đồng:**

* **GET** `/community/feed`
* **POST** `/community/posts` (chia sẻ thành tựu; nên hạn chế text tự do nếu trẻ nhỏ)
* **GET** `/community/posts/{postId}`
* **POST** `/community/posts/{postId}/comments`
* **POST** `/community/posts/{postId}/reactions`
* **POST** `/community/posts/{postId}/report`

**Giáo viên & lớp học:**

* **POST** `/classes` (teacher tạo lớp)
* **GET** `/classes`
* **POST** `/classes/{classId}/students` (mời/thêm học sinh bằng mã)
* **GET** `/classes/{classId}/progress` (tổng quan tiến độ cả lớp)
* **POST** `/assignments` (giao bài: quiz/game)
* **GET** `/assignments?classId=`
* **POST** `/assignments/{assignmentId}/publish`

---

## 11) Thông báo (Push/In-app)

* **POST** `/devices` (đăng ký FCM/APNs token)
* **GET** `/notifications`
* **POST** `/notifications/{id}/read`

---

## 12) Content Management (nếu có backend quản trị)

* **POST** `/admin/content` (tạo game/quiz/activity)
* **PATCH** `/admin/content/{id}`
* **POST** `/admin/question-bank/import` (import CSV/JSON)
* **POST** `/admin/assets/upload` → trả signed URL upload

---

## Phân công nhiệm vụ

## 1) Các chức năng cơ bản

### 1.1. Tài khoản & hồ sơ trẻ (Profiles)

* Đăng ký/đăng nhập (Phụ huynh/Giáo viên).
* Phụ huynh tạo **nhiều hồ sơ trẻ**: tên, tuổi, avatar, lớp/độ tuổi.
* Chọn hồ sơ trước khi học/chơi.
* Phân quyền theo vai trò: parent/teacher/child.

### 1.2. Kho nội dung học (Catalog)

* Danh mục nội dung theo:

  * Loại: **Game / Quiz / Màu sắc / Số đếm**
  * Chủ đề: Toán, Khoa học, Nghệ thuật…
  * Độ tuổi/level
* Xem chi tiết bài học/trò chơi (mô tả, xem trước, level, mục tiêu).

### 1.3. Trò chơi giáo dục (Game)

* Các mini game rèn:

  * logic (ghép hình, tìm quy luật)
  * quyết định (chọn đáp án nhanh)
  * kỹ năng xã hội (tình huống đơn giản, lựa chọn hành vi)
* Cơ chế điểm/thưởng: sao, huy hiệu, streak học tập.
* Lưu tiến trình phiên chơi (session): thời gian, số lỗi, điểm.

### 1.4. Câu đố & trắc nghiệm (Quiz)

* Bộ câu hỏi theo chủ đề/level.
* Làm bài theo lượt (attempt), chấm điểm, hiển thị đúng/sai + giải thích đơn giản.
* Thống kê kết quả theo ngày/tuần.

### 1.5. Hoạt động học màu sắc

* Nhận biết & phân biệt màu:

  * chọn đúng màu theo yêu cầu
  * ghép màu – vật thể
  * tô màu đơn giản (basic)
* Có âm thanh/hiệu ứng khích lệ khi làm đúng.

### 1.6. Hoạt động học số đếm

* Đếm số lượng vật thể, chọn số đúng, kéo-thả số vào nhóm.
* Học khái niệm cơ bản: ít/nhiều, lớn/nhỏ (mức cơ bản).

### 1.7. Phản hồi tương tác & động lực học

* Phản hồi tức thì: "Giỏi lắm!", "Thử lại nhé!"
* Hệ thống phần thưởng: huy hiệu theo mốc.
* Ghi chú tiến bộ (cho phụ huynh/giáo viên).

### 1.8. An toàn & bảo mật cho trẻ

* Không hiển thị nội dung nhạy cảm.
* Có **Parental control**:

  * giới hạn thời gian học/ngày
  * tắt/bật cộng đồng
  * lọc nội dung theo độ tuổi
* Bảo mật dữ liệu tài khoản (token, HTTPS).

### 1.9. Cộng đồng & giáo viên (mức cơ bản)

* Giáo viên tạo lớp, xem tiến độ tổng quan.
* Trẻ/phụ huynh có thể chia sẻ "thành tựu" (achievement post) trong phạm vi kiểm soát.
* Report nội dung xấu (nếu có post/comment).

---

## 2) Các chức năng AI (nếu có)


### AI-1. Gợi ý nội dung học cá nhân hóa

* Dựa trên: độ tuổi, level, lịch sử làm sai, chủ đề yếu → gợi ý bài tiếp theo.
* Có thể làm bản đơn giản (rule-based) nhưng gọi là "AI gợi ý" vẫn ổn nếu có logic phân tích dữ liệu.

### AI-2. Lọc nội dung cộng đồng

* Nếu có post/comment: dùng API moderation để lọc từ độc hại/spam.
* Có thể bật/tắt theo parental setting.

### AI-3 (tuỳ chọn). Trợ lý học tập

* Hỏi đáp đơn giản: "Số 5 lớn hơn số 3 không?" → chatbot trả lời theo kịch bản.
* Làm offline/rule-based cũng được.

---

## 3) Các Actors của hệ thống

* **Child (Trẻ em):** học/chơi nội dung, làm quiz, nhận thưởng.
* **Parent (Phụ huynh):** tạo hồ sơ trẻ, xem tiến độ, đặt giới hạn, duyệt/tắt cộng đồng.
* **Teacher (Giáo viên):** tạo lớp, giao bài, xem thống kê lớp.
* **Admin (Quản trị – tùy chọn):** quản lý nội dung (quiz/game), duyệt report cộng đồng.

---

## 4) Công nghệ sử dụng để phát triển ứng dụng

**Mobile (Android):**

* Android Studio, **Java**
* UI: XML + RecyclerView, Material Components
* Kiến trúc: MVVM nhẹ (Repository + ViewModel + LiveData)
* Network: Retrofit2 + OkHttp + Gson
* Lưu trữ:

  * SharedPreferences/EncryptedSharedPreferences (token)
  * Room (cache catalog/tiến trình offline) *tuỳ chọn*
* Push notification (tuỳ chọn): Firebase Cloud Messaging (FCM)

**Backend (chọn 1 hướng)**

* Hướng A (nhanh cho BTL): **Firebase** (Auth + Firestore + Storage)
* Hướng B (chuẩn REST): Node/NestJS hoặc Spring Boot + MySQL/PostgreSQL

---

## 5) API bên ngoài (nếu có)

Tuỳ theo backend bạn chọn:

### Nếu dùng REST backend

* **FCM**: push notification (nhắc học, thông báo giáo viên)
* **Cloud Storage**: S3/Firebase Storage (ảnh, âm thanh, asset trò chơi)
* **Moderation API** (nếu có cộng đồng): lọc toxic/spam

### Nếu dùng Firebase

* Firebase Auth, Firestore, Storage, Analytics/Crashlytics (tuỳ chọn)

---

## 6) Bản phân công công việc của từng cá nhân


Chuẩn rồi — **chia theo “chiều ngang” = chia theo *API module* (feature slice)** sẽ công bằng hơn vì **mỗi người làm end-to-end**: Firestore collections + Cloud Functions (nếu có) + Security Rules phần của mình + Android UI gọi đúng API + test + viết phần báo cáo tương ứng.

Dưới đây là phương án mình thấy **hợp lý và cân bằng nhất** với lựa chọn của bạn: **BE Firebase + AI trợ lý học tập dùng API**.

---

## Các API/Module chính của BTL (để chia ngang)

Mình coi “API” gồm 2 loại:

* **Firestore CRUD** (coi như API dữ liệu)
* **Cloud Functions (HTTPS Callable)** (API nghiệp vụ/AI)

**Firestore collections (cốt lõi):**

* `users/{uid}` (role, name)
* `children/{childId}` (parentUid, profile, settings)
* `contents/{contentId}` (type, ageRange, level, assetsRef…)
* `quizzes/{quizId}/questions/{questionId}`
* `attempts/{attemptId}` (childId, quizId, answers, result)
* `sessions/{sessionId}` (childId, contentId, score, duration…)
* `classes/{classId}` + `assignments/{assignmentId}`
* `achievements/{childId}/badges/{badgeId}` (hoặc badges embedded)
* `assistant_threads/{threadId}` + `assistant_messages/{msgId}` (tuỳ lưu lịch sử chat)

**Cloud Functions đề xuất:**

* `assistantChat(childId, threadId, message)` → gọi API trợ lý học tập
* `gradeAttempt(attemptId)` → chấm quiz
* `startSession(childId, contentId)` → tạo session
* `completeSession(sessionId, payload)` → cập nhật session + thưởng
* `joinClassByCode(code, childId)` *(khuyến nghị để join an toàn hơn)*
* `getProgressSummary(childId, range)` *(tổng hợp nhanh — optional)*

---


| API module                                                                            | Thành viên 1                                                                                                                                             | Thành viên 2                                                                                                                                                                    | Thành viên 3                                                                                                                                        | Thành viên 4                                                                                                                                                  |
| ------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1) **Auth & User Role API** (Firebase Auth + `users`)                                 | **Firebase Auth (register/login/logout), tạo `users/{uid}` + role; điều hướng UI theo role; viết rules phần `users`; test account**                      | -                                                                                                                                                                               | -                                                                                                                                                   | -                                                                                                                                                             |
| 2) **Children & Settings API** (`children`)                                           | -                                                                                                                                                        | **CRUD `children`; settings: timeLimit, enableAI, enableCommunity; UI chọn hồ sơ trẻ; rules cho `children`; test dữ liệu**                                                      | -                                                                                                                                                   | -                                                                                                                                                             |
| 3) **Content Catalog API** (`contents` + Storage refs)                                | -                                                                                                                                                        | -                                                                                                                                                                               | **Danh sách/lọc `contents` theo type/age/level/topic; màn catalog + detail; tải assets từ Storage (ảnh/âm thanh); rules đọc `contents`; seed data** | -                                                                                                                                                             |
| 4) **Activities API** (Color & Counting + results)                                    | -                                                                                                                                                        | -                                                                                                                                                                               | **2 activity màn hình (Color + Counting) + lưu kết quả vào Firestore (vd `activity_results` hoặc nhúng trong `sessions`); UI feedback; test**       | -                                                                                                                                                             |
| 5) **Sessions & Rewards API** (`sessions` + `achievements`, Functions start/complete) | -                                                                                                                                                        | **Cloud Functions `startSession`, `completeSession` (tính điểm/thưởng); ghi `sessions`; cập nhật `achievements`; rules cho sessions/achievements; màn “Kết quả & nhận thưởng”** | -                                                                                                                                                   | -                                                                                                                                                             |
| 6) **Quiz & Attempts API** (`quizzes/questions`, `attempts`, Function grade)          | -                                                                                                                                                        | -                                                                                                                                                                               | -                                                                                                                                                   | **Hiển thị quiz/questions; tạo `attempts`, lưu answers; Cloud Function `gradeAttempt`; UI kết quả/giải thích; rules cho quiz/attempts; seed quiz data**       |
| 7) **Class & Assignment API** (`classes`, `assignments`, Function join code)          | **Teacher tạo class, tạo mã lớp; Function `joinClassByCode`; tạo assignments; màn Teacher (class/assignment); rules cho classes/assignments; test join** | -                                                                                                                                                                               | -                                                                                                                                                   | -                                                                                                                                                             |
| 8) **AI Learning Assistant API** (Function `assistantChat` + thread/messages)         | -                                                                                                                                                        | -                                                                                                                                                                               | -                                                                                                                                                   | **Cloud Function `assistantChat` gọi API trợ lý; prompt kid-safe; lưu `assistant_threads/messages`; Android UI chat + context theo bài đang học; test & log** |
| 9) **Progress Dashboard API** (query/tổng hợp)                                        | -                                                                                                                                                        | **Màn Progress (ngày/tuần); query từ `sessions` + `attempts` + `achievements`; (optional) Function `getProgressSummary`; test bằng dữ liệu thật**                               | -                                                                                                                                                   | -                                                                                                                                                             |

### Vì sao chia thế này “công bằng”?

* Mỗi người đều có **API dữ liệu + logic + UI** (không ai chỉ làm mỗi backend hoặc mỗi UI).
* Mỗi người có **1–2 Cloud Functions** (hoặc nghiệp vụ tương đương) + 2–3 màn hình Android.
* Khối lượng “nặng” (Quiz + AI) được gom cùng 1 người, nhưng đổi lại người đó **không phải làm class/teacher + progress**.

---

## Deliverables bắt buộc để “ai cũng nộp bài” (mỗi người 1 gói)

Mỗi thành viên nộp đúng phần mình phụ trách, gồm:

1. **Code Android** (màn hình + gọi Firebase/Function đúng module của mình)
2. **Code Firebase (nếu module có)**: rules snippet + Cloud Function file của module
3. **Tài liệu API module (1–2 trang)**:

   * Collections liên quan + field chính
   * Function input/output (nếu có)
   * Luồng UI gọi API
4. **Bằng chứng chạy**: 5–10 ảnh screenshot hoặc 1 video ngắn

---

## Quy ước phối hợp để đỡ “đá nhau” (siêu thực tế)

* Mỗi module có 1 file doc: `docs/api-module-<name>.md` (do người phụ trách viết)
* Security Rules:

  * Mỗi người viết **phần rules của module mình**
  * Thành viên 1 (Auth/Class) là người **merge rules cuối** (chỉ merge, không “làm hộ”)
* Data seed: ai phụ trách module nào thì tự seed collection đó (contents/quiz/class…)

---

Nếu bạn gửi mình **tên 4 thành viên** (hoặc ký hiệu A/B/C/D), mình sẽ thay trực tiếp vào bảng trên để bạn đem nộp luôn.


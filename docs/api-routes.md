# API Routes Reference

> **Mục đích**: tham khảo nhanh toàn bộ API + **thứ tự chạy bắt buộc** cho từng luồng end-to-end.
>
> **Cập nhật**: 2026-07-13 — sau khi triển khai GĐ1 (SubTask core), GĐ2 (Submission v2), GĐ3 (Review + Roll-up), và chuẩn hóa RESTful.
>
> **Base URL**: `http://localhost:8386` (mặc định theo `application.properties`)
>
> **Swagger UI**: `http://localhost:8386/swagger-ui.html`
>
> **Auth**: Tất cả endpoint (trừ `/api/auth/**`) yêu cầu JWT bearer token trong header `Authorization: Bearer <token>`.

---

## 0. Quick navigation

| Tag | Prefix | Mục đích |
|---|---|---|
| [Account](#1-account--auth) | `/api/auth/**`, `/api/account-requests/**`, `/api/accounts/**`, `/api/admin/accounts/**` | Đăng ký, OTP, đăng nhập, phê duyệt role |
| [Project & Plan](#2-project--plan-workflow) | `/api/workflow/projects/**`, `/api/workflow/production-plans/**` | Tạo Project, activate, xem dashboard Plan |
| [Chapter & Task](#3-chapter--task-workflow) | `/api/workflow/chapters/**`, `/api/workflow/tasks/**` | Tạo Chapter (auto 4 Task default), giao Task, feedback |
| [SubTask](#4-subtask-workflow-mangaka--assistant) | `/api/tasks/{taskId}/subtasks`, `/api/subtasks/**`, `/api/users/{userId}/subtasks` | Mangaka chia SubTask cho Assistant |
| [Submission](#5-submission-workflow-assistant--mangaka--tantō) | `/api/submissions/**`, `/api/subtasks/{subTaskId}/submissions`, `/api/tasks/{taskId}/submissions` | Upload file, xem history, review |

---

## 1. Account & Auth

> Mount: `controller/AccountController.java`

| # | Method | Path | Body / Params | Role | Mô tả |
|---|---|---|---|---|---|
| A1 | `POST` | `/api/auth/accounts` | `AccountRequest` (email, password, otpCode, requestedRole, ...) | public | Đăng ký tài khoản mới — status = PENDING chờ duyệt |
| A2 | `POST` | `/api/auth/send-otp` | `SendOtpRequest { email }` | public | Gửi mã OTP qua email |
| A3 | `POST` | `/api/auth/login` | `AccountLoginRequest { email, password }` | public | Đăng nhập → trả JWT token |
| A4 | `GET` | `/api/account-requests?status=PENDING` | — | ADMIN, MANAGER | Danh sách tài khoản chờ duyệt |
| A5 | `POST` | `/api/account-requests/{accountId}/approve` | — | ADMIN, MANAGER | Duyệt → set role từ `requestedRole`, status = ACTIVE |
| A6 | `POST` | `/api/account-requests/{accountId}/reject` | `RejectAccountRequest { reason }` | ADMIN, MANAGER | Từ chối kèm lý do |
| A7 | `POST` | `/api/admin/accounts/{accountId}/activate` | — | ADMIN, MANAGER | Kích hoạt lại account |
| A8 | `POST` | `/api/admin/accounts/{accountId}/deactivate` | — | ADMIN, MANAGER | Vô hiệu hóa account |
| A9 | `GET` | `/api/admin/accounts` | — | ADMIN | Danh sách tất cả accounts |
| A10 | `GET` | `/api/accounts/{accountId}` | — | any | Chi tiết 1 account |
| A11 | `GET` | `/api/accounts/search?email=...` | — | any | Tìm account theo email |

### 1.1 Thứ tự chuẩn bị môi trường (lần đầu)

```
Bước A1 → A2 → A3
   ↓       ↓     ↓
register  OTP  login
  (PENDING)     (nhận token)

Sau khi đã có ADMIN account (từ DataInitializer):
Bước A4 → A5    (lặp lại cho từng Assistant/Mangaka/Tantou mới)
        hoặc
        A6     (nếu cần từ chối)
```

Bootstrap (chạy tự động khi khởi động `DataInitialized`):
- `admin@gmail.com` / `admin123` (ADMIN)
- `tantou@manga.com` / `password123` (TANTOU_EDITOR)
- `mangaka@manga.com` / `password123` (MANGAKA)
- `assistant1@manga.com` / `password123` (ASSISTANT)
- `assistant2@manga.com` / `password123` (ASSISTANT)
- `assistant3@manga.com` / `password123` (ASSISTANT)
- `board1@manga.com` / `password123` (EDITORIAL_BOARD_MEMBER)
- `board2@manga.com` / `password123`
- `board3@manga.com` / `password123`

---

## 2. Project & Plan Workflow

> Mount: `controller/ProductionWorkflowController.java` (prefix `/api/workflow`)

| # | Method | Path | Body / Params | Role | Mô tả |
|---|---|---|---|---|---|
| P1 | `POST` | `/api/workflow/projects?editorId={boardMemberId}` | `CreateProjectRequest { title, genre, targetAudience, format, tantouId }` | EDITORIAL_BOARD_MEMBER | Tạo Project, gán Tantō |
| P2 | `PUT` | `/api/workflow/projects/{projectId}/status?tantouId={tantouId}` | `UpdateProjectStatusRequest { status: ACTIVE }` | TANTOU_EDITOR | Activate Project → **tự động tạo empty ProductionPlan (status = PLANNING)** |
| P3 | `GET` | `/api/workflow/production-plans/{planId}/dashboard?requesterId={userId}` | — | TANTOU_EDITOR, MANGAKA | Dashboard toàn bộ Plan (chapters, tasks, %) |

### 2.1 Ràng buộc

- **P1**: `editorId` phải có role `EDITORIAL_BOARD_MEMBER`. `tantouId` phải có role `TANTOU_EDITOR`.
- **P2**: Chỉ Tantō mới activate được. Status chỉ chuyển `DRAFT → ACTIVE`. Lần đầu ACTIVE = sinh `ProductionPlan`.
- **P3**: Dashboard trả về: `planId, projectId, startDate, endDate, totalVolumeTarget, planStatus, completionPercentage, chapters[].tasks[]`.

### 2.2 Thứ tự chuẩn

```
(EDITOR) P1  → Project tồn tại ở status DRAFT
(TANTOU) P2  → Project ACTIVE  + ProductionPlan auto sinh
(anyone)  P3  → Lấy planId để dùng cho các bước sau
```

---

## 3. Chapter & Task Workflow

> Mount: `controller/ProductionWorkflowController.java`

| # | Method | Path | Body / Params | Role | Mô tả |
|---|---|---|---|---|---|
| C1 | `POST` | `/api/workflow/chapters?requesterId={tantouId}` | `CreateChapterRequest { planId, chapterNumber, title, targetPageCount, startDate, endDate, publishDate? }` | TANTOU_EDITOR | Tạo Chapter trong Plan |
| C2 | `POST` | `/api/workflow/chapters/{chapterId}/assign` | `AssignChapterRequest { mangakaId, requesterId }` | TANTOU_EDITOR | Gán Chapter cho Mangaka → **auto set Chapter.owner + gán tất cả Task con cho Mangaka** |
| C3 | `PUT` | `/api/workflow/chapters/{chapterId}/status?status={status}&requesterId={requesterId}` | query param `status=COMPLETED` | TANTOU_EDITOR | Đổi trạng thái Chapter. **Rule**: không thể COMPLETED nếu vẫn còn Task !DONE |
| T1 | `PUT` | `/api/workflow/tasks/{taskId}/status` | `UpdateTaskStatusRequest { requesterId, status }` | TANTOU, MANGAKA, ASSISTANT | Đổi status Task (TODO/IN_PROGRESS/REVIEW/DONE). **Lock rule**: REVIEW chỉ Tantō mới thoát |
| T2 | `POST` | `/api/workflow/tasks/{taskId}/feedback` | `CreateFeedbackRequest { createdById, content, attachmentUrl?, decision: APPROVED|REJECTED }` | TANTOU_EDITOR | Tantō feedback trên Task → APPROVED = DONE; REJECTED = IN_PROGRESS |
| T3 | `POST` | `/api/workflow/tasks/{taskId}/assign` | `AssignTaskRequest { assigneeId, requesterId, deadline }` | TANTOU (assign to Mangaka), MANGAKA (assign INKING/BACKGROUND to Assistant) | Giao Task cho user |
| A1 | `GET` | `/api/workflow/projects/{projectId}/assets?requesterId={userId}` | — | any | List assets của project |

### 3.1 Auto-task generation

Khi `POST /chapters` thành công, hệ thống **TỰ ĐỘNG** tạo 4 Task default trong Chapter:

| Thứ tự | TaskType | Mô tả |
|---|---|---|
| 1 | `NAME_WIP` | Đặt tên + vẽ nháp tên chapter |
| 2 | `LINEART` | Vẽ line art |
| 3 | `INKING` | Inking (tô mực) |
| 4 | `BACKGROUND` | Vẽ background |

Mỗi Task tạo ra ở `taskWorkflowStatus = TODO`, chưa có deadline, chưa có assignee.

### 3.2 Thứ tự chuẩn

```
(TANTOU) C1  → Chapter tồn tại + 4 Task default sinh ra
(TANTOU) C2  → Chapter.owner = Mangaka + TẤT CẢ Task.assignee = Mangaka
(TANTOU) T3  → Set deadline từng Task (1 request / Task)
(MANGAKA) T1  → Update status: TODO → IN_PROGRESS → REVIEW
(TANTOU) T2  → Feedback: APPROVED (DONE) hoặc REJECTED (back to IN_PROGRESS)
```

---

## 4. SubTask Workflow (Mangaka ↔ Assistant)

> Mount: `controller/SubTaskController.java` (KHÔNG có class-level prefix → dùng full path)

| # | Method | Path | Body / Params | Role | Mô tả |
|---|---|---|---|---|---|
| S1 | `POST` | `/api/tasks/{taskId}/subtasks` | `CreateSubTaskRequest { requesterId, assigneeId, title, description?, productionTaskType?, deadlineDate, deadlineTime? }` | MANGAKA (của task đó), TANTOU | Mangaka chia Task → SubTask cho Assistant |
| S2 | `GET` | `/api/tasks/{taskId}/subtasks?requesterId={userId}` | — | any | List SubTask của 1 Task |
| S3 | `GET` | `/api/users/{userId}/subtasks?requesterId={requesterId}` | — | any | List SubTask của Assistant (Assistant chỉ thấy của mình) |
| S4 | `GET` | `/api/subtasks/{subTaskId}?requesterId={userId}` | — | assignee/Mangaka/Tantō | Chi tiết 1 SubTask |
| S5 | `POST` | `/api/subtasks/{subTaskId}/reopen?requesterId={userId}` | — | MANGAKA, TANTOU | Reopen SubTask COMPLETED → NEEDS_REVISION |

### 4.1 Ràng buộc quan trọng

- **S1 — Deadline validation**: `subtask.deadlineDate (+time)` PHẢI ≤ `task.deadline`. Vi phạm → 400.
- **S1 — Assignee role**: `assigneeId` PHẢI có role `ASSISTANT`, nếu không → 400.
- **S1 — Status**: Task cha KHÔNG được là `DONE`. Nếu `DONE` → 400.
- **S1 — Mangaka scope**: Mangaka chỉ được chia Task mà mình là assignee. Nếu khác → 403.
- **S3 — Self-scope**: Assistant chỉ xem được SubTask của mình. Nếu khác → 403.
- **S5**: Chỉ reopen được khi SubTask đang `COMPLETED`. Nếu khác → 400.

### 4.2 Trạng thái SubTask

```
TODO ──→ IN_PROGRESS ──→ SUBMITTED (sau khi Assistant upload ROUGH_SKETCH)
                              │
                              ├──→ APPROVED ROUGH/REVISION ─→ IN_PROGRESS
                              │     (để Assistant submit FINAL)
                              │
                              ├──→ REJECTED ──→ NEEDS_REVISION
                              │     (Assistant submit REVISION mới)
                              │
                              └──→ APPROVED FINAL ──→ COMPLETED
                                    (Mangaka review qua POST /submissions/{id}/reviews)
```

### 4.3 Thứ tự chuẩn

```
(TANTOU)    T3  → set deadline Task cho Mangaka
(MANGAKA)   S1  → chia SubTask { assigneeId = Assistant, deadlineDate ≤ Task.deadline }
              ↓
        SubTask.status = TODO
              ↓
         (chờ Assistant upload file → SUB5 ở §5)
```

---

## 5. Submission Workflow (Assistant → Mangaka → Tantō)

> Mount: `controller/SubmissionController.java` (KHÔNG có class-level prefix → full path)

| # | Method | Path | Body / Params | Role | Mô tả |
|---|---|---|---|---|---|
| SUB1 | `POST` | `/api/submissions` (multipart) | `CreateSubmissionRequest { requesterId, subTaskId\|taskId, submissionType, note?, files[] }` | ASSISTANT (subTask), MANGAKA (taskLevel) | Upload file lên SubTask hoặc Task |
| SUB2 | `GET` | `/api/submissions` | — | any | List toàn bộ submissions (admin/debug) |
| SUB3 | `GET` | `/api/submissions/{submissionId}` | — | any | Chi tiết 1 submission |
| SUB4 | `DELETE` | `/api/submissions/{submissionId}` | — | admin | Xóa submission |
| SUB5 | `POST` | `/api/submissions/{submissionId}/reviews` | `ReviewSubmissionRequest { reviewerId, decision, note? }` | MANGAKA (subTask), TANTOU (Task-level) | Review (approve/reject) — **cascades xuống SubTask status** |
| SUB6 | `GET` | `/api/subtasks/{subTaskId}/submissions` | — | any | History 1 SubTask (newest first) |
| SUB7 | `GET` | `/api/tasks/{taskId}/submissions` | — | any | History TASK_LEVEL submissions của 1 Task |

### 5.1 `submissionType` cycle (rule chain)

```
Lần đầu của SubTask        → ROUGH_SKETCH (bắt buộc)
ROUGH_SKETCH bị REJECTED   → REVISION (parent = REJECTED submission)
ROUGH_SKETCH được APPROVED → FINAL (yêu cầu ít nhất 1 ROUGH_SKETCH approved)
TASK_LEVEL                 → gọi khi tất cả SubTask COMPLETED (chỉ Mangaka)
```

### 5.2 Rule chain validation (server-side, trả 400 nếu vi phạm)

| Tình huống | Lỗi |
|---|---|
| Upload với 0 file | `"At least one file must be uploaded"` |
| Không truyền `subTaskId`/`taskId` HOẶC truyền cả 2 | `"Exactly one of taskId or subTaskId must be supplied"` |
| Assistant nộp ROUGH_SKETCH nhưng không phải assigned SubTask | 403 |
| SubTask đang SUBMITTED cố upload ROUGH_SKETCH mới | 400 |
| SubTask COMPLETED cố upload | 400 + reopen trước |
| FINAL khi chưa có ROUGH_SKETCH APPROVED | 400 |
| REVISION nhưng chưa từng có parent REJECTED | 400 |
| TASK_LEVEL nhưng còn SubTask ! COMPLETED | 400 |

### 5.3 Review cascade (sau SUB5)

```
   Submission.status = APPROVED + SubTask round
   ────────────────────────────────────────────────
   ROUGH_SKETCH/REVISION ──→ SubTask.status = IN_PROGRESS   (để Assistant nộp FINAL)
   FINAL                  ──→ SubTask.status = COMPLETED
                              → Task.progressPercentage +1/N
                              → recompute ProductionPlan.completionPercentage

   Submission.status = REJECTED (note bắt buộc)
   ────────────────────────────────────────────────
   SubTask.status = NEEDS_REVISION
   (Assistant phải upload REVISION mới, parent auto-link = submission REJECTED này)

   Submission.status = APPROVED + Task-level (TASK_LEVEL)
   ────────────────────────────────────────────────
   Task.status         = DONE
   Task.progress       = 100%
   Plan.completion     += 1 chapter done
```

### 5.4 Ràng buộc quan trọng

- **SUB1**: `requesterId` phải trùng với SubTask.assignee (nếu là SubTask round).
- **SUB5 (SubTask round)**: reviewer phải là Mangaka assignee của parent Task.
- **SUB5 (Task-level)**: reviewer phải có role `TANTOU_EDITOR`.
- **SUB5**: REJECTED mà không có `note` → 400.

---

## 6. End-to-end flows (thứ tự chạy bắt buộc)

### 6.1. Flow Setup — chuẩn bị tài khoản (lần đầu)

```text
Bước                API                              Ghi chú
────                ───                              ────────
0. Boot             DataInitialized chạy tự động      Tạo ADMIN, board, tantou, mangaka, 3 assistant
1. Login admin      A3 /api/auth/login                Lưu token admin vào biến tokenAdmin
2. (tuỳ chọn) Đăng ký user mới qua flow public
   a. Send OTP      A2 /api/auth/send-otp
   b. Register      A1 /api/auth/accounts (status=PENDING)
   c. Duyệt         A4 /api/account-requests?status=PENDING → A5 approve
3. Login user mới   A3 → lưu tokenXxx
```

Sau bước này ta có:
- `tokenAdmin` (ADMIN)
- `tokenTantou` (TANTOU_EDITOR)
- `tokenBoard` (EDITORIAL_BOARD_MEMBER)
- `tokenMangaka` (MANGAKA)
- `tokenAssistant1`, `tokenAssistant2`, `tokenAssistant3` (ASSISTANT)

---

### 6.2. Flow A — Project → Plan → Chapter → Task (Tantō + Mangaka)

```text
Bước   Vai trò      API                                                Body / Param                                                            Kết quả
─────  ────────      ───                                                ────────────                                                            ─────────
1      Board Member  P1  POST /api/workflow/projects?editorId={boardId}  CreateProjectRequest { title, format: WEEKLY_SHONEN, tantouId }      Project.id=10 (status=DRAFT)
2      Tantō         P2  PUT  /api/workflow/projects/10/status?tantouId  UpdateProjectStatusRequest { status: ACTIVE }                            Project.status=ACTIVE + ProductionPlan auto-id=1
3      Tantō         P3  GET  /api/workflow/production-plans/1/dashboard                                                                  Lưu planId=1
4      Tantō         C1  POST /api/workflow/chapters?requesterId         CreateChapterRequest { planId:1, chapterNumber:1, title, startDate, endDate, targetPageCount:24 }   Chapter.id=20 + 4 Task default (id=101..104)
5      Tantō         C2  POST /api/workflow/chapters/20/assign           AssignChapterRequest { mangakaId, requesterId }                         Chapter.owner=mangaka + 4 Task.assignee=mangaka
6      Tantō         T3  POST /api/workflow/tasks/101/assign (× 4)       AssignTaskRequest { assigneeId=mangaka, requesterId=tantou, deadline }   Task có deadline ngày+giờ
```

Kết quả: Project ACTIVE, Plan PLANNING, Chapter với 4 Task đã được giao cho Mangaka cùng deadline.

---

### 6.3. Flow B — Từ Task đến SubTask (Mangaka → Assistant)

```text
Bước   Vai trò      API                                                Body                                                                                          Kết quả
─────  ────────      ───                                                ────────────                                                                                  ─────────
1      Mangaka       S1  POST /api/tasks/101/subtasks                  CreateSubTaskRequest { requesterId=mangaka, assigneeId=assistant1, title:"Line art 1-12",
                                                                    deadlineDate, deadlineTime }                                                                  SubTask.id=500 (status=TODO)
2      Mangaka       S1  POST /api/tasks/101/subtasks                  Tương tự cho Inking, Background, ...                                                            SubTask.id=501, 502, 503
3      Assistant     S3  GET  /api/users/300/subtasks?requesterId=300                                                                    Xem các SubTask mình được giao (sắp xếp theo deadline)
4      Mangaka       S2  GET  /api/tasks/101/subtasks?requesterId                                                                       Xem tất cả SubTask của 1 Task
5      Mangaka       S5  POST /api/subtasks/500/reopen?requesterId                                                                       (Khi cần reopen SubTask COMPLETED → NEEDS_REVISION)
```

---

### 6.4. Flow C — Submission đầy đủ 1 SubTask (Assistant upload → Mangaka review, lặp lại)

```text
Bước   Vai trò      API                                                                              Body                                                                              Kết quả
─────  ────────      ───                                                                              ────────────                                                                        ─────────
1      Assistant     SUB1 POST /api/submissions (multipart)                                            CreateSubmissionRequest { requesterId=assistant1, subTaskId:500,
                                                                                                    submissionType: ROUGH_SKETCH, note:"Bản nháp đầu", files[]:"sketch_v1.psd" }      Submission.id=900 (version=1, status=PENDING),
                                                                                                                                                                            SubTask.status=SUBMITTED
2      Mangaka       SUB6 GET  /api/subtasks/500/submissions                                           (xem history, round nào PENDING)
3      Mangaka       SUB5 POST /api/submissions/900/reviews                                            ReviewSubmissionRequest { reviewerId=mangaka, decision: REJECTED, note:"Line 5-8 quá mờ" }
                                                                                                                                                                            SubTask.status=NEEDS_REVISION
4      Assistant     SUB1 POST /api/submissions                                                        CreateSubmissionRequest { requesterId=assistant1, subTaskId:500,
                                                                                                    submissionType: REVISION, ... }                                                       Submission.id=901 (version=2, parent=900,
                                                                                                                                                                            SubTask.status=SUBMITTED)
5      Mangaka       SUB5 POST /api/submissions/901/reviews                                            ReviewSubmissionRequest { reviewerId=mangaka, decision: APPROVED, note:"" }       SubTask.status=IN_PROGRESS (Assistant được nộp FINAL)
6      Assistant     SUB1 POST /api/submissions                                                        CreateSubmissionRequest { ..., submissionType: FINAL, files[]:"final.psd" }     Submission.id=902 (version=3, status=PENDING),
                                                                                                                                                                            SubTask.status=SUBMITTED
7      Mangaka       SUB5 POST /api/submissions/902/reviews                                            ReviewSubmissionRequest { reviewerId=mangaka, decision: APPROVED }                  SubTask.status=COMPLETED,
                                                                                                                                                                            Task.progress +=25%
8      ... lặp lại cho SubTask 501, 502, 503 ...
```

---

### 6.5. Flow D — Task-level submission (Mangaka → Tantō)

```text
Bước   Vai trò      API                                                                              Body                                                                              Kết quả
─────  ────────      ───                                                                              ────────────                                                                        ─────────
1      Mangaka       verify precondition: GET /api/tasks/101/subtasks?requesterId                                                                            Cả 4 SubTask phải COMPLETED, Task.progress=100%
2      Mangaka       SUB1 POST /api/submissions                                                        CreateSubmissionRequest { requesterId=mangaka, taskId:101,
                                                                                                    submissionType: TASK_LEVEL, files[]:"chapter1_compilation.psd" }                Submission.id=950 (version=1, status=PENDING)
3      Tantō         SUB7 GET  /api/tasks/101/submissions                                                                                                          xem history
4      Tantō         SUB5 POST /api/submissions/950/reviews                                            ReviewSubmissionRequest { reviewerId=tantou, decision: APPROVED }                  Task.status=DONE,
                                                                                                                                                                            Plan.completionPercentage += 1/4
5      Tantō         C3  PUT  /api/workflow/chapters/20/status?status=COMPLETED&requesterId                                                                       Chapter.completed (cần check Task đã DONE)
```

---

### 6.6. Flow E — Mangaka feedback Task (legacy, dùng T2)

> Dùng khi Task chưa được chia SubTask (Mangaka tự làm).

```text
Bước   Vai trò      API                                                Body                                                          Kết quả
─────  ────────      ───                                                ────────────                                                  ─────────
1      Mangaka       T1  PUT /api/workflow/tasks/102/status             UpdateTaskStatusRequest { requesterId=mangaka, status:REVIEW } Task.status=REVIEW (locked)
2      Tantō         T2  POST /api/workflow/tasks/102/feedback          CreateFeedbackRequest { createdById=tantou, content, decision: APPROVED }  Task.status=DONE
        hoặc  REJECTED → Task.status=IN_PROGRESS
```

---

### 6.7. Flow F — Đăng ký Assistant mới trong khi hệ thống đã chạy

```text
Bước   Vai trò      API                                                          Body                                          Kết quả
─────  ────────      ───                                                          ────────────                                  ─────────
1      (any)         A2 POST /api/auth/send-otp                                   { email }
2      (any)         A1 POST /api/auth/accounts                                   AccountRequest { ..., requestedRole: ASSISTANT } status=PENDING
3      Admin/Manager A4 GET  /api/account-requests?status=PENDING                                                      Thấy account mới
4      Admin/Manager A5 POST /api/account-requests/{accountId}/approve                                                status=ACTIVE, role=ASSISTANT
5      Mangaka       có thể dùng ngay A11 GET /api/accounts/search?email=... để lấy userId cho S1
```

---

## 7. Thứ tự khởi tạo hệ thống (lần đầu tiên)

```text
0. Khởi động SQL Server + chạy V1..V4 migrations (theo thứ tự)
1. Chạy backend Spring Boot — DataInitialized tự tạo 9 account bootstrap
2. Login bằng admin@gmail.com / admin123 để lấy token admin
3. (nếu cần) Tạo Tantō/Mangaka/Assistant mới qua flow §6.1
4. Sẵn sàng cho các flow nghiệp vụ §6.2 trở đi
```

---

## 8. Status codes chuẩn

| Code | Nghĩa | Khi nào |
|---|---|---|
| 200 | OK | GET / PUT / DELETE thành công |
| 201 | Created | POST tạo resource mới thành công |
| 400 | Bad Request | WorkflowRuleViolation / IllegalArgument / IllegalState |
| 403 | Forbidden | AccessDenied — sai role hoặc không phải chủ sở hữu |
| 404 | Not Found | ResourceNotFoundException / EntityNotFoundException |
| 409 | Conflict | Legacy endpoint khi xung đột |
| 500 | Internal Error | Lỗi không mong đợi |

---

## 9. Quick test scenarios

### 9.1 Smoke test end-to-end (Postman / curl)

```bash
# 1. Login Tantō
curl -X POST http://localhost:8386/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"tantou@manga.com","password":"password123"}'
# Save the JWT in TOKEN_TANTOU

# 2. Tạo Project (cần token Board)
curl -X POST 'http://localhost:8386/api/workflow/projects?editorId=BOARD_ID' \
  -H "Authorization: Bearer $TOKEN_BOARD" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Manga","genre":"Action","format":"WEEKLY_SHONEN","tantouId":TANTOU_ID}'

# 3. Activate
curl -X PUT 'http://localhost:8386/api/workflow/projects/10/status?tantouId=TANTOU_ID' \
  -H "Authorization: Bearer $TOKEN_TANTOU" \
  -H "Content-Type: application/json" \
  -d '{"status":"ACTIVE"}'

# 4. Lấy planId
curl 'http://localhost:8386/api/workflow/production-plans/1/dashboard?requesterId=TANTOU_ID' \
  -H "Authorization: Bearer $TOKEN_TANTOU"
```

### 9.2 Các test case quan trọng cần verify thủ công

| # | Test | Expected |
|---|---|---|
| 1 | Mangaka cố assign Task deadline trước ngày hiện tại | 400 |
| 2 | Assistant upload submission không có file | 400 "At least one file must be uploaded" |
| 3 | Mangaka REJECTED submission không có note | 400 "rejection note is required" |
| 4 | Mangaka tạo SubTask deadline SAU Task deadline | 400 |
| 5 | SubTask ở NEEDS_REVISION → Assistant upload ROUGH_SKETCH mới | 400 (phải là REVISION) |
| 6 | SubTask FINAL khi chưa có ROUGH_SKETCH APPROVED | 400 |
| 7 | Mangaka khác (không phải Task assignee) review submission | 403 |
| 8 | Task 100% SubTask COMPLETED → progress = 100% | đúng |
| 9 | Tantō APPROVED TASK_LEVEL → Plan.completion += 1/N chapter | đúng |
| 10 | Assistant cố GET `/api/users/{otherUserId}/subtasks` | 403 |

---

## 10. Endpoint tổng số (tham khảo nhanh)

| Tag | Số endpoint | Path gốc |
|---|---|---|
| Account & Auth | 11 | `/api/auth/**`, `/api/account-requests/**`, `/api/accounts/**`, `/api/admin/accounts/**` |
| Project & Plan | 3 | `/api/workflow/projects/**`, `/api/workflow/production-plans/**` |
| Chapter & Task | 7 | `/api/workflow/chapters/**`, `/api/workflow/tasks/**` |
| SubTask | 5 | `/api/tasks/{taskId}/subtasks`, `/api/subtasks/**`, `/api/users/{userId}/subtasks` |
| Submission | 7 | `/api/submissions/**`, `/api/subtasks/{subTaskId}/submissions`, `/api/tasks/{taskId}/submissions` |
| **Tổng** | **33** | |

---

## 11. Tài liệu liên quan

- `docs/plans/submission-workflow-plan.md` — Plan chi tiết + V4 SQL + design decisions (bao gồm cả Risk & Rollback).
- `CONFIGURATION.md` — Biến môi trường + setup PowerShell.
- `SKETCH_WORKFLOW_API.md` — API cho luồng sketch (legacy, riêng).

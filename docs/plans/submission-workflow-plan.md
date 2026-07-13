# Plan: Task → SubTask → Submission (Production Plan)

> **Scope**: Triển khai luồng nghiệp vụ Tantō → Mangaka → Assistant thông qua
> Production Plan, Task, SubTask, Submission (polymorphic), SubmissionFile, Feedback (đã có).
>
> **Author**: Plan generated from BA analysis
> **Date**: 2026-07-12
> **Stack**: Java 21 · Spring Boot 4.0.6 · Spring Data JPA · SQL Server
> **Prereq**: V2 (`production_workflow_schema.sql`) + V3 (`project_member_schema.sql`) đã có.

---

## 0. Tóm tắt bối cảnh codebase

### 0.1 Những gì ĐÃ có (qua V2, V3, code modified)

| Hạng mục | File / Cột | Trạng thái |
|---|---|---|
| `Project.format`, `Project.genre`, `Project.targetAudience`, `Project.projectStatus` | V2 | ✅ migrate xong |
| `ProductionPlan.startDate/endDate/totalVolumeTarget/planStatus` | V2 | ✅ |
| `Chapter.targetPageCount/publishDate/chapterStatus` | V2 | ✅ |
| `Task.productionTaskType/acceptanceCriteria/taskStatus/assignee_id` | V2 | ✅ |
| Bảng `feedbacks` (Tantō feedback trên Task) | V2 | ✅ + đã có Controller/Service |
| Bảng `assets` (resource của Project) | V2 | ✅ |
| `ProjectRole` + `ProjectMember` (join table Account ↔ Project) | V3 | ✅ + Repository đã có |
| `Account`, `SystemRole`, JWT, Notification | (legacy) | ✅ |
| Workflow Service mới: `ProductionWorkflowService` | (new) | ✅ — đã có `createProject/activateProject/createChapter/assignChapter/updateTaskStatus/createFeedback/assignTask/getPlanDashboard/getProjectAssets` |
| `MangaWorkflowService` cũ (Name Submission + Board vote) | (legacy) | ✅ giữ nguyên |
| `SubmissionService` cũ (theo `Planning`, `Project`) | (legacy) | ⚠️ vẫn còn, **chưa tích hợp** với workflow mới |

### 0.2 Những gì CHƯA có so với BA

| # | Gap | Ảnh hưởng |
|---|---|---|
| G1 | **Chưa có bảng `sub_tasks`** | Assistant không có chỗ nhận việc con từ Mangaka |
| G2 | **Submission chưa polymorphic** — bảng `Submission` hiện gắn với `Project`/`Planning`, không gắn với `Task`/`SubTask` | Không thể submit file lên SubTask/Task |
| G3 | **Chưa có `SubmissionFile.file_type`** (rough/revision/final) + **thiếu `order`** | Không phân biệt được file theo vòng |
| G4 | **`submission_type` + `version` + `parent_submission_id` chưa có** trên `Submission` | Không có chuỗi version theo thời gian |
| G5 | **`reviewer_id`, `reviewed_at` chưa có** trên `Submission` (đã có entity `SubmissionReview` cũ nhưng chỉ dùng cho Name submission 2-stage) | Không audit được người duyệt |
| G6 | **Deadline Task hiện là `Instant`** (1 trường), chưa tách `deadline_date` + `deadline_time` | Khớp tới yêu cầu BA — cần nâng cấp |
| G7 | **Chưa có SubTask deadline validation** (≤ task.deadline) | Không enforce |
| G8 | **Chưa có cờ `is_overdue` (computed)** | UI không highlight được |
| G9 | **Chưa có `progress_percentage` cached trên Task/Plan** | Tính lại mỗi query → chậm khi scale |
| G10 | **`feedbacks` gắn với `Task`** — cần rẽ nhánh để Mangaka review SubTask (Assistant nộp bài) | Hiện chỉ Tantō feedback Task được; chưa có Mangaka review submission của Assistant |
| G11 | **Chưa có cron / scheduled job** để nhắc deadline | Non-functional |
| G12 | **Không có DTO SubTask**, **không có DTO Submission v2** | Thiếu API contract |
| G13 | **`SubmissionService` cũ bind vào `Planning`** (entity cũ — sắp bị thay thế bằng ProductionPlan) | Có thể xung đột khi ProductionPlan dashboard dùng `planningId` |

---

## 1. Phân tích nghiệp vụ (đã thẩm tra lại qua code)

### 1.1 Actors (đã ánh xạ role hệ thống)

| Vai trò trong BA | System role (SystemRoleName) | Vai trò thật |
|---|---|---|
| Tantō / Biên tập | `TANTOU_EDITOR` | Lập Plan, giao Task, duyệt Submission cấp Task |
| Mangaka | `MANGAKA` | Nhận Task, chia SubTask, review Submission của Assistant |
| Assistant | `ASSISTANT` | Nhận SubTask, nộp Submission |
| Editorial Board | `EDITORIAL_BOARD_MEMBER` | (Workflow cũ — name submission, **không thuộc** luồng này) |

**Lưu ý từ code**: `ProductionWorkflowServiceImpl` đang dùng đúng 3 role trên:
```java
requester.hasRole(SystemRoleName.TANTOU_EDITOR)
mangaka.hasRole(SystemRoleName.MANGAKA)
requester.hasRole(SystemRoleName.MANGAKA) || hasRole(ASSISTANT)
```

### 1.2 Luồng nghiệp vụ (đã verify qua code)

```
[1] Tantō tạo Project → ProductionPlan (auto)         ── createProject() đã có
[2] Tantō activate Project                              ── activateProject() đã có
[3] Tantō tạo Chapter trong Plan                         ── createChapter() đã có
    → Auto tạo 4 Task default: NAME_WIP, LINEART, INKING, BACKGROUND
       (xem createChapter - line 173 V2)
[4] Tantō giao Task cho Mangaka                          ── assignTask() đã có
    → Validate deadline ∈ [chapter.start, chapter.end]
    → Set Task.assignee = Mangaka
[5] Mangaka chia Task thành nhiều SubTask                  ── ★ CHƯA CÓ → phải build
    → Validate deadline SubTask ≤ deadline Task cha
    → Set SubTask.assignee = Assistant
[6] Assistant nộp Submission (file PSD) cho SubTask        ── ★ CHƯA CÓ → phải build
    → submission_type = ROUGH_SKETCH (lần đầu)
    → version = 1
    → status = PENDING_REVIEW
    → SubTask.status: TODO/IN_PROGRESS → SUBMITTED
[7] Mangaka review Submission:
    7a. APPROVED (rough) → SubTask về IN_PROGRESS, Assistant có thể nộp FINAL
    7b. REJECTED         → SubTask chuyển NEEDS_REVISION,
                           Assistant nộp version mới (parent_submission_id)
    7c. APPROVED (final) → SubTask → COMPLETED,
                           update Task.progress
[8] Tất cả SubTask COMPLETED → cho phép Mangaka nộp Submission cấp Task
    → submission_type = TASK_LEVEL
    → status = PENDING_REVIEW, Tantō review cuối
[9] Tantō APPROVED Submission cấp Task
    → Task → COMPLETED
    → Plan cập nhật completion_percentage
[10] Cron job check Task/SubTask deadline (24h, 2h, overdue) → Notification
```

### 1.3 Trạng thái — đối chiếu code

| State | Code hiện tại | Đề xuất |
|---|---|---|
| **Submission** | `SubmissionStatus`: `PENDING, SUBMITTED, PROCESSING, PENDING_BOARD_REVIEW, ON_GOING, APPROVED, REJECTED` (cũ — thừa) | Rút gọn: `PENDING_REVIEW, APPROVED, REJECTED` + thêm `submissionType: ROUGH_SKETCH, REVISION, FINAL, TASK_LEVEL` |
| **Task (cấp workflow)** | `TaskWorkflowStatus`: `TODO, IN_PROGRESS, REVIEW, DONE` (đã có trên Task — **nhưng đặt nhầm**: vì Task là "đơn vị việc của Mangaka") | Giữ nguyên giá trị, **thêm 2 trạng thái** cho SubTask: `NEEDS_REVISION`, `COMPLETED`. **Tách enum `SubTaskWorkflowStatus`** |
| **Chapter** | `ChapterStatus`: `BACKLOG, IN_PRODUCTION, COMPLETED, PUBLISHED` | Giữ nguyên |
| **Plan** | `PlanStatus`: `PLANNING, IN_PROGRESS, PAUSED` | Giữ nguyên |
| **Project** | `ProjectWorkflowStatus`: `DRAFT, ACTIVE, ON_HOLD, COMPLETED` | Giữ nguyên |

### 1.4 Polymorphic Submission — quyết định thiết kế

Vì cùng 1 bảng `Submission` phải trỏ về `Task` HOẶC `SubTask`, ta chọn **2 FK + check constraint SQL Server** thay vì (type, id) để:
- Vẫn có **FK constraint** đảm bảo toàn vẹn tham chiếu.
- Truy vấn nhanh theo từng loại (đã có index sẵn).
- Hibernate xử lý được (2 `@ManyToOne` optional).

```sql
submittable_task_id    BIGINT NULL,   -- FK -> Task(Id)  khi submission cho Task
submittable_subtask_id BIGINT NULL,   -- FK -> SubTask(Id) khi submission cho SubTask
-- CHECK chỉ 1 trong 2 NOT NULL
CONSTRAINT CK_submission_polymorphic CHECK (
    (submittable_task_id IS NOT NULL AND submittable_subtask_id IS NULL) OR
    (submittable_task_id IS NULL AND submittable_subtask_id IS NOT NULL)
)
```

So với đề xuất `submittable_type + submittable_id` (đa hình kiểu STI), cách này:
- ✅ Vẫn cho phép polymorphic ở tầng service (`SubmissionResponse` chỉ fill 1 field).
- ✅ Database-level FK đảm bảo integrity.
- ✅ Dễ query hơn với SQL Server.

---

## 2. Backlog (đã đối chiếu code)

| US | Nội dung | Phụ thuộc kỹ thuật |
|---|---|---|
| **US-01** | Tantō tạo Task cho chapter + giao Mangaka + deadline | DB OK, **thiếu `production_plan_id` FK trên Task** (xem §3, G1.5) — **code hiện gắn Task với `Chapter`, KHÔNG trực tiếp với Plan** — đề xuất chấp nhận vì route qua Chapter là chính |
| **US-02** | Mangaka list Task theo deadline | `TaskRepository.findByAssigneeIdOrderByDeadlineAsc(...)` — chưa có |
| **US-03** | Mangaka chia SubTask | ❌ **Phải build mới** |
| **US-04** | Assistant list SubTask | ❌ |
| **US-05** | Assistant nộp ROUGH_SKETCH | ❌ |
| **US-06** | Assistant nộp REVISION (parent link) | ❌ |
| **US-07** | Assistant nộp FINAL | ❌ |
| **US-08** | Mangaka review Submission (approve/reject + note) | ❌ (Feedback đã có nhưng chỉ cho Tantō review Task) |
| **US-09** | Mangaka reopen SubTask đã COMPLETED | ❌ |
| **US-10** | Mangaka tổng hợp Submission cấp Task (sau khi mọi SubTask COMPLETED) | ❌ |
| **US-11** | Cron nhắc deadline (24h, 2h, overdue) | ❌ |

---

## 3. Kế hoạch triển khai

### 3.1 Tổng quan — 4 giai đoạn (mỗi giai đoạn ship được, có thể demo FE)

| Giai đoạn | Phạm vi | Deliverable | PR gợi ý |
|---|---|---|---|
| **GĐ1 — DB & SubTask core** | V4 migration, `SubTask` entity, repo, SubTaskService, SubTaskController | Mantaka tạo + Assistant xem SubTask | 1 PR |
| **GĐ2 — Submission polymorphic** | Thêm cột lên `Submission`, đổi FK sang polymorphic, `SubmissionFile.file_type/order`, SubmissionService v2, controller v2 | Assistant upload, Mangaka view history | 1 PR |
| **GĐ3 — Review & Roll-up** | `submitFeedback` mở rộng cho cả SubTask; cơ chế approve/reject; roll-up progress %, deadline validation | Mangaka review, auto-update Task % + Plan % | 1 PR |
| **GĐ4 — Task-level submission + Cron** | Submission cấp Task, Tantō review cuối, Cron job đẩy Notification trước/sau deadline | Đóng luồng end-to-end + job schedule | 1 PR |

### 3.2 Giai đoạn 1 — SubTask core

#### 3.2.1 Migration V4 (`V4__subtask_and_submission_polymorphic.sql`)

```sql
-- 1. Tạo bảng SubTask
CREATE TABLE SubTask (
    Id                 BIGINT IDENTITY(1,1) NOT NULL,
    parent_task_id     BIGINT NOT NULL,
    assignee_id        BIGINT NULL,
    title              NVARCHAR(255) NULL,
    description        NVARCHAR(MAX) NULL,
    production_task_type NVARCHAR(50) NULL,         -- LINEART / INKING / BACKGROUND...
    subtask_status     NVARCHAR(50) NOT NULL DEFAULT 'TODO',
                                            -- TODO|IN_PROGRESS|SUBMITTED|NEEDS_REVISION|COMPLETED
    deadline_date      DATE            NULL,
    deadline_time      TIME(0)         NULL,        -- HH:mm
    version            INT             NOT NULL DEFAULT 1,
    created_at         DATETIME2       NOT NULL DEFAULT GETDATE(),
    updated_at         DATETIME2       NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_SubTask PRIMARY KEY (Id),
    CONSTRAINT FK_SubTask_Task     FOREIGN KEY (parent_task_id) REFERENCES Task(Id),
    CONSTRAINT FK_SubTask_Assignee FOREIGN KEY (assignee_id)    REFERENCES Account(id)
);
CREATE INDEX IX_SubTask_Parent ON SubTask(parent_task_id);
CREATE INDEX IX_SubTask_Assignee ON SubTask(assignee_id);

-- 2. Bổ sung trường deadline tách date/time cho Task (nếu muốn đồng nhất)
--    Cân nhắc giữ Instant deadline cũ. Nếu tách, để nullable cho không phá data.
ALTER TABLE Task ADD deadline_date DATE NULL, deadline_time TIME(0) NULL;

-- 3. Biến Submission thành polymorphic
ALTER TABLE Submission ADD
    submittable_task_id     BIGINT NULL,
    submittable_subtask_id  BIGINT NULL,
    submission_type         NVARCHAR(50) NOT NULL DEFAULT 'TASK_LEVEL',
                                     -- ROUGH_SKETCH|REVISION|FINAL|TASK_LEVEL
    version                 INT         NOT NULL DEFAULT 1,
    parent_submission_id    BIGINT NULL,
    reviewer_id             BIGINT NULL,
    reviewed_at             DATETIME2   NULL;

ALTER TABLE Submission ADD CONSTRAINT FK_Submission_SubTask
    FOREIGN KEY (submittable_subtask_id) REFERENCES SubTask(Id);
ALTER TABLE Submission ADD CONSTRAINT FK_Submission_Reviewer
    FOREIGN KEY (reviewer_id) REFERENCES Account(id);
ALTER TABLE Submission ADD CONSTRAINT FK_Submission_Parent
    FOREIGN KEY (parent_submission_id) REFERENCES Submission(Id);

ALTER TABLE Submission DROP CONSTRAINT FK_Submission_Project;  -- xem §3.2.4
ALTER TABLE Submission DROP COLUMN ProjectId;

ALTER TABLE Submission ADD CONSTRAINT CK_Submission_Polymorphic CHECK (
    (submittable_task_id IS NOT NULL AND submittable_subtask_id IS NULL) OR
    (submittable_task_id IS NULL AND submittable_subtask_id IS NOT NULL)
);

CREATE INDEX IX_Sub_Submittable ON Submission(submittable_task_id, version);
CREATE INDEX IX_Sub_Submittable_ST ON Submission(submittable_subtask_id, version);

-- 4. SubmissionFile bổ sung
ALTER TABLE SubmissionFile ADD
    file_type NVARCHAR(50) NULL,  -- ROUGH_SKETCH|REVISION|FINAL|COMPILATION
    file_order INT NULL;
CREATE INDEX IX_SF_Submission ON SubmissionFile(submission_id, file_order);
```

> **Lưu ý FK hiện tại** (`FK_Submission_Project`): Vì `Submission` entity đang trỏ `Project` (`@JoinColumn ProjectId`), Hibernate validate sẽ fail khi drop cột. **Hai lựa chọn**:
> 1. DROP cột trước, cập nhật entity (xem §3.2.2) → chạy lại.
> 2. Giữ cột nullable (không drop), thêm check constraint — deprecated nhưng an toàn cho rollout.

**Quyết định**: Lựa chọn 1 (drop + đổi entity), dọn dẹp từ đầu. Vì schema đang `validate` và dữ liệu Submission hiện không nhiều (chỉ có demo).

#### 3.2.2 Entity mới + cập nhật

**Mới:** `SubTask.java`

```java
@Entity @Table(name = "SubTask")
@Getter @Setter
public class SubTask {
    @Id @GeneratedValue(strategy = IDENTITY) Long id;

    @ManyToOne(fetch=LAZY) @JoinColumn(name="parent_task_id", nullable=false)
    @JsonBackReference Task task;

    @ManyToOne(fetch=LAZY) @JoinColumn(name="assignee_id")
    Account assignee;                          // Assistant

    @Column(length=255) String title;
    @Lob String description;

    @Enumerated(STRING) @Column(name="subtask_status", length=50)
    SubTaskWorkflowStatus subtaskStatus = SubTaskWorkflowStatus.TODO;

    @Enumerated(STRING) @Column(name="production_task_type", length=50)
    TaskType productionTaskType;

    @Column(name="deadline_date") LocalDate deadlineDate;
    @Column(name="deadline_time") LocalTime deadlineTime;

    @Column(name="version") Integer version = 1;

    Instant createdAt = Instant.now();
    Instant updatedAt = Instant.now();

    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }

    /** Computed (transient) - dùng cho UI. */
    @Transient Instant getDeadlineInstant() {
        if (deadlineDate == null) return null;
        LocalTime t = deadlineTime != null ? deadlineTime : LocalTime.MAX;
        return deadlineDate.atTime(t).atZone(ZoneId.systemDefault()).toInstant();
    }
    @Transient boolean isOverdue() {
        Instant d = getDeadlineInstant();
        return d != null
            && subtaskStatus != SubTaskWorkflowStatus.COMPLETED
            && d.isBefore(Instant.now());
    }

    @OneToMany(mappedBy="subTask", cascade=ALL, orphanRemoval=true)
    @JsonManagedReference("subtask-submissions")
    List<Submission> submissions;
}

enum SubTaskWorkflowStatus {
    TODO, IN_PROGRESS, SUBMITTED, NEEDS_REVISION, COMPLETED
}
```

**Sửa:** `Submission.java`

```java
// Bỏ @JoinColumn ProjectId
// Bỏ @JoinColumn PlanningId   (giữ nếu muốn audit cho Name submission cũ, optional)
@ManyToOne(fetch=LAZY) @JoinColumn(name="submittable_task_id")
@JsonBackReference("sub-task") Task task;

@ManyToOne(fetch=LAZY) @JoinColumn(name="submittable_subtask_id")
@JsonBackReference("sub-subtask") SubTask subTask;

@Enumerated(STRING) @Column(name="submission_type", length=50)
SubmissionType submissionType;   // ROUGH_SKETCH|REVISION|FINAL|TASK_LEVEL

@Column(name="version", nullable=false) Integer version = 1;

@ManyToOne(fetch=LAZY) @JoinColumn(name="parent_submission_id")
@JsonBackReference Submission parent;

@ManyToOne(fetch=LAZY) @JoinColumn(name="reviewer_id")
Account reviewer;

@Column(name="reviewed_at") Instant reviewedAt;
```

**Sửa:** `SubmissionFile.java` — thêm `file_type`, `file_order`:

```java
@Enumerated(STRING) @Column(name="file_type", length=50)
FileType fileType;           // ROUGH_SKETCH|REVISION|FINAL|COMPILATION

@Column(name="file_order") Integer fileOrder;
```

**Mới:** enum `FileType`, `SubmissionType`.

#### 3.2.3 Repositories

```java
public interface SubTaskRepository extends JpaRepository<SubTask, Long> {
    List<SubTask> findByTaskId(Long taskId);
    List<SubTask> findByAssigneeIdOrderByDeadlineDateAsc(Long assigneeId);
    boolean existsByTaskIdAndSubtaskStatusNot(Long taskId, SubTaskWorkflowStatus st);
    long countByTaskIdAndSubtaskStatus(Long taskId, SubTaskWorkflowStatus st);
}

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findBySubTaskIdOrderByVersionDesc(Long subTaskId);
    List<Submission> findByTaskIdOrderByVersionDesc(Long taskId);

    @Query("""
       SELECT MAX(s.version) FROM Submission s
       WHERE ( (:taskId IS NOT NULL AND s.task.id = :taskId)
            OR (:subTaskId IS NOT NULL AND s.subTask.id = :subTaskId) )
       """)
    Optional<Integer> findMaxVersion(Long taskId, Long subTaskId);
}
```

#### 3.2.4 Service `SubTaskService`

```java
public interface SubTaskService {
    SubTaskResponse createSubTask(Long taskId, CreateSubTaskRequest req);    // US-03
    List<SubTaskResponse> listByTask(Long taskId);
    List<SubTaskResponse> listByAssignee(Long assigneeId);                   // US-04
    SubTaskResponse getById(Long id);
    SubTaskResponse reopen(Long id, Long requesterId);                       // US-09
}
```

**Validate quan trọng:**
```java
// Deadline SubTask ≤ deadline Task cha
if (sub.deadlineInstant.isAfter(task.deadlineInstant))
    throw new IllegalArgumentException("SubTask deadline must be ≤ parent Task deadline");

// SubTask không tạo được nếu Task đã COMPLETED
if (task.taskWorkflowStatus == DONE) throw new IllegalStateException("Parent task is DONE");
```

**Validate quyền (RBAC):**
- Tạo SubTask: chỉ Mangaka có role MANGAKA **và** là assignee của Task cha (hoặc Tantō).
- Xem SubTask: Assistant chỉ thấy của mình; Mangaka thấy của Task mình quản; Tantō thấy tất cả trong Project.

#### 3.2.5 Controller `SubTaskController`

```java
@RestController @RequestMapping("/api/subtasks")
@Tag(name = "SubTask", description = "Mangaka creates sub-tasks, Assistant receives")
@RequiredArgsConstructor
public class SubTaskController {

    @PostMapping("/tasks/{taskId}")
    public ResponseEntity<ResponseBase> create(@PathVariable Long taskId,
                                               @Valid @RequestBody CreateSubTaskRequest req) {...}

    @GetMapping
    public ResponseEntity<ResponseBase> list(@RequestParam Long requesterId,
                                             @RequestParam(required=false) Long taskId,
                                             @RequestParam(required=false) Long assigneeId) {...}

    @GetMapping("/{id}")
    public ResponseEntity<ResponseBase> get(@PathVariable Long id) {...}

    @PostMapping("/{id}/reopen")
    public ResponseEntity<ResponseBase> reopen(@PathVariable Long id,
                                               @RequestParam Long requesterId) {...}
}
```

#### 3.2.6 DTO mới

- `CreateSubTaskRequest`: `requesterId`, `assigneeId`, `title`, `productionTaskType`, `deadlineDate`, `deadlineTime`, `description?`.
- `SubTaskResponse`: `id`, `taskId`, `assigneeId/Name`, `title`, `subtaskStatus`, `productionTaskType`, `deadlineDate`, `deadlineTime`, `isOverdue`, `currentSubmissionId`, `currentVersion`, `version` (SubTask).

#### 3.2.7 Acceptance test GĐ1

| Case | Expected |
|---|---|
| Mangaka tạo SubTask với deadline > Task deadline | 400 + msg |
| Mangaka tạo SubTask gán cho user không phải Assistant | 400 |
| Assistant GET `/api/subtasks?assigneeId=me` | chỉ thấy của mình |
| SubTask overdue → response `isOverdue=true` | OK |

---

### 3.3 Giai đoạn 2 — Submission polymorphic

#### 3.3.1 Nghiệp vụ chính

**Assistant nộp file** (`POST /api/submissions`)
- Form-data: `subTaskId`, `submissionType=ROUGH_SKETCH`, `note?`, `files[]`.
- Server:
  1. Lookup SubTask, check `assignee = me`.
  2. Check SubTask chưa COMPLETED.
  3. Validate `submissionType` hợp lệ theo rule chain:
     - Lần đầu của SubTask → phải `ROUGH_SKETCH`.
     - Có parent (cuối cùng REJECTED) → phải `REVISION`.
     - Đã có ROUGH_SKETCH approved → được `FINAL`.
  4. Tính `version = max(prev) + 1`.
  5. Link `parent_submission_id` nếu là REVISION.
  6. Lưu Submission + SubmissionFile với `fileOrder` tăng dần.
  7. Cập nhật SubTask status: `TODO/IN_PROGRESS → SUBMITTED` (nếu ROUGH_SKETCH).

**Mangaka review** (tạm thời dùng `Feedback` cũ, mở rộng sang SubTask)
- Xem lịch sử Submission của SubTask theo `version DESC`.
- Duyệt / yêu cầu sửa thông qua endpoint mới `POST /api/submissions/{id}/review` (xem GĐ3).

#### 3.3.2 Repository bổ sung

```java
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findBySubTaskIdOrderByVersionDesc(Long subTaskId);
    List<Submission> findByTaskIdOrderByVersionDesc(Long taskId);

    @Query("""
       SELECT s FROM Submission s
       WHERE s.subTask.id = :subTaskId AND s.submissionType = com.mm.model.SubmissionType.ROUGH_SKETCH
         AND s.status = com.mm.model.SubmissionStatus.APPROVED
       ORDER BY s.version DESC
       """)
    List<Submission> findApprovedRoughForSubTask(Long subTaskId);
}
```

#### 3.3.3 Service `SubmissionService` v2

```java
public interface SubmissionService {
    SubmissionResponse create(Long requesterId, CreateSubmissionRequest req);    // upload
    SubmissionResponse review(Long id, ReviewSubmissionRequest req);              // approve/reject (GĐ3)
    List<SubmissionResponse> historyBySubTask(Long subTaskId);
    List<SubmissionResponse> historyByTask(Long taskId);
}
```

Chi tiết `create`:

```java
@Transactional
public SubmissionResponse create(Long requesterId, CreateSubmissionRequest req) {
    Account submitter = accountRepo.findById(requesterId).orElseThrow(...);
    SubTask subTask = subTaskRepo.findById(req.getSubTaskId()).orElseThrow(...);

    // RBAC: chỉ Assistant được giao SubTask này
    if (!subTask.getAssignee().getId().equals(submitter.getId())
        || !submitter.hasRole(ASSISTANT))
        throw new AccessDeniedException("Only assigned Assistant can submit");

    if (subTask.getSubtaskStatus() == COMPLETED)
        throw new IllegalStateException("SubTask already COMPLETED");

    // Rule chain
    SubmissionType type = SubmissionType.valueOf(req.getSubmissionType());
    validateSubmissionType(subTask, type);            // (xem dưới)

    // version
    int version = submissionRepo.findMaxVersion(null, subTask.getId()).orElse(0) + 1;

    Submission s = new Submission();
    s.setSubTask(subTask);
    s.setSubmissionType(type);
    s.setVersion(version);
    s.setSubmittedBy(submitter);
    s.setStatus(SubmissionStatus.PENDING_REVIEW);
    s.setSubmittedAt(Instant.now());

    if (type == REVISION) s.setParent(prevPending);

    s = submissionRepo.save(s);

    // Files
    saveFiles(s, req.getFiles(), type);
    return mapToResponse(s);
}
```

#### 3.3.4 Controller `SubmissionController` v2

| Method | Endpoint | Mô tả |
|---|---|---|
| `POST` | `/api/submissions` (multipart) | Tạo submission, upload file |
| `GET` | `/api/submissions/history?subTaskId=X` | Lịch sử 1 SubTask |
| `GET` | `/api/submissions/history?taskId=X` | Lịch sử 1 Task (cấp Task-level) |
| `POST` | `/api/submissions/{id}/review` | (GĐ3) Approve/Reject |

#### 3.3.5 DTO

```java
class CreateSubmissionRequest {
    Long requesterId;            // hoặc lấy từ SecurityContextHolder
    Long subTaskId;              // 1 trong 2: subTaskId hoặc taskId
    Long taskId;
    SubmissionType submissionType;
    String note;
    List<MultipartFile> files;   // ≥ 1
}
class ReviewSubmissionRequest {
    Long reviewerId;
    SubmissionStatus decision;   // APPROVED | REJECTED
    String note;                 // required khi REJECTED
}
class SubmissionResponse {
    Long id;
    Long subTaskId;
    Long taskId;
    SubmissionType submissionType;
    Integer version;
    Long parentSubmissionId;
    SubmissionStatus status;
    Long submittedById; String submittedByName;
    Instant submittedAt;
    Long reviewerId; Instant reviewedAt;
    String note;
    List<SubmissionFileResponse> files;
}
```

#### 3.3.6 Acceptance test GĐ2

| Case | Expected |
|---|---|
| Assistant upload 0 file | 400 "At least 1 file required" |
| Assistant upload, không phải ROUGH_SKETCH đầu tiên | 400 "First submission must be ROUGH_SKETCH" |
| Assistant khác (không phải assignee) cố submit | 403 |
| SubTask đang SUBMITTED → Assistant nộp ROUGH_SKETCH tiếp | 409 / 400 |
| Submission REVISION không có parent PENDING_REVIEW →  | 400 |

---

### 3.4 Giai đoạn 3 — Review & Roll-up

#### 3.4.1 Nghiệp vụ

| Endpoint | Logic |
|---|---|
| `POST /api/submissions/{id}/review` body `{decision, note}` | Nếu `APPROVED` → set `status=APPROVED, reviewer_id, reviewed_at`; **nếu `submissionType=FINAL`** → SubTask → `COMPLETED`. Nếu `REJECTED` → SubTask → `NEEDS_REVISION` |
| (computed) `Task.progressPercentage` | `countSubTaskStatus(COMPLETED) / total * 100` |
| (computed) `Plan.completionPercentage` | `countChaptersDone / totalChapters * 100` |
| Deadline validation trên Assistant (đã có trong assignTask), bổ sung cho SubTask (xem §3.2.4) |

#### 3.4.2 ReviewService

```java
@Transactional
public SubmissionResponse review(Long submissionId, ReviewSubmissionRequest req) {
    Account reviewer = accountRepo.findById(req.getReviewerId()).orElseThrow(...);
    Submission s = submissionRepo.findById(submissionId).orElseThrow(...);

    // RBAC: người review phải là Mangaka quản Task cha SubTask
    if (!reviewer.hasRole(MANGAKA))
        throw new AccessDeniedException(...);

    if (req.getDecision() == REJECTED && (req.getNote() == null || req.getNote().isBlank()))
        throw new IllegalArgumentException("Note is required when rejecting");

    s.setStatus(req.getDecision());
    s.setReviewer(reviewer);
    s.setReviewedAt(Instant.now());
    s.setReviewNote(req.getNote());

    if (s.getSubmissionType() == SubmissionType.FINAL
        && req.getDecision() == SubmissionStatus.APPROVED) {
        SubTask st = s.getSubTask();
        st.setSubtaskStatus(SubTaskWorkflowStatus.COMPLETED);
        subTaskRepo.save(st);
        recomputeTaskProgress(st.getTask().getId());
    } else if (req.getDecision() == SubmissionStatus.REJECTED) {
        SubTask st = s.getSubTask();
        st.setSubtaskStatus(SubTaskWorkflowStatus.NEEDS_REVISION);
        subTaskRepo.save(st);
    }
    submissionRepo.save(s);
    return mapToResponse(s);
}
```

#### 3.4.3 Roll-up helper

```java
private void recomputeTaskProgress(Long taskId) {
    Task task = taskRepo.findById(taskId).orElseThrow(...);
    long total = subTaskRepo.countByTaskId(taskId);
    long done = subTaskRepo.countByTaskIdAndSubtaskStatus(taskId, COMPLETED);
    int pct = total == 0 ? 0 : (int)((done * 100.0) / total);
    task.setProgress(pct);
    if (pct == 100) task.setTaskWorkflowStatus(DONE);   // optional auto-complete
    taskRepo.save(task);

    // Roll-up lên Plan
    if (task.getChapter() != null) {
        recomputePlanProgress(task.getChapter().getProductionPlan().getId());
    }
}

private void recomputePlanProgress(Long planId) {
    long total = chapterRepo.countByPlanId(planId);
    long done = chapterRepo.countByPlanIdAndStatus(planId, COMPLETED, PUBLISHED);
    ProductionPlan plan = ...;
    plan.setCompletionPercentage(total == 0 ? 0 : (int)((done*100.0)/total));
    planRepo.save(plan);
}
```

#### 3.4.4 Acceptance test GĐ3

| Case | Expected |
|---|---|
| Mangaka APPROVED FINAL → SubTask.COMPLETED, Task.progress +25% | OK |
| Mangaka REJECTED ROUGH_SKETCH không note | 400 |
| Cả 4 SubTask COMPLETED → Task.progress = 100 | OK |
| Task 100% → Chapter status tự IN_PRODUCTION (hoặc giữ rule cũ cần Tantō duyệt) | theo spec cũ: giữ rule Tantō duyệt |

---

### 3.5 Giai đoạn 4 — Task-level submission & Cron

#### 3.5.1 Nghiệp vụ

- `POST /api/tasks/{id}/submit` (multipart): Mangaka nộp Submission cấp Task.
  - Validate: mọi SubTask của Task đã COMPLETED.
  - Tạo `Submission{type=TASK_LEVEL, task=…, …}`.
  - SubTask status → `SUBMITTED` (Task vẫn `REVIEW`).
- `POST /api/submissions/{id}/review` đã có — cho phép Tantō APPROVED.
  - Nếu APPROVED → Task → `COMPLETED`.
  - Notification cho các bên.

#### 3.5.2 Cron job

- Class `DeadlineReminderJob` với annotation `@Scheduled`.
- Mỗi 30 phút (configurable):
  - SubTask/Task chưa COMPLETED, deadline trong khoảng `[now+23.5h, now+24.5h]` → nhắc "24h".
  - SubTask/Task chưa COMPLETED, deadline trong khoảng `[now+1.5h, now+2.5h]` → nhắc "2h".
  - SubTask/Task quá deadline → đánh `isOverdue=true` (transient, computed), gửi notification.
- Cần enable `@EnableScheduling` trên main class.

#### 3.5.3 Acceptance test GĐ4

| Case | Expected |
|---|---|
| Còn SubTask chưa xong, cố submit Task-level | 400 |
| Tantō APPROVED Task-level Submission | Task.COMPLETED, Plan progress +1 chapter |
| Cron chạy với Task sắp đến hạn | Notification được lưu |

---

## 4. File-by-file Implementation Order

> Best practice: **mỗi giai đoạn viết 1 PR riêng**, test riêng, merge riêng.

### 4.1 PR-GĐ1: SubTask core
**Diff files**:

1. `database/migrations/V4__subtask_and_submission_polymorphic.sql` *(tách nhỏ 1.sql phần §3.2.1)*.
2. `model/SubTask.java` (mới).
3. `model/SubTaskWorkflowStatus.java` (mới).
4. `repository/SubTaskRepository.java` (mới).
5. `dto/request/CreateSubTaskRequest.java` (mới).
6. `dto/response/SubTaskResponse.java` (mới).
7. `service/interfaces/SubTaskService.java` (mới).
8. `service/impl/SubTaskServiceImpl.java` (mới).
9. `controller/SubTaskController.java` (mới).
10. `model/Submission.java` (sửa: thêm 2 FK polymorphic).
11. `model/SubmissionFile.java` (sửa: thêm `file_type`/`file_order`).
12. `model/SubmissionType.java`, `model/FileType.java` (mới).
13. `repository/SubmissionRepository.java` (sửa: thêm hàm polymorphic).

### 4.2 PR-GĐ2: Submission polymorphic
**Diff files**:

1. `dto/request/CreateSubmissionRequest.java` (mới).
2. `dto/response/SubmissionResponse.java` (mới, thay thế `Submission` exposed API).
3. `dto/request/ReviewSubmissionRequest.java` (mới).
4. `service/interfaces/SubmissionService.java` (đổi signature).
5. `service/impl/SubmissionServiceImpl.java` (rewrite).
6. `controller/SubmissionController.java` (rewrite endpoints).
7. Xử lý back-compat: giữ `POST /api/submissions/{userId}` cũ hoặc deprecate (xem §5.2).

> **Note** – sau khi REST audit (GĐ4-final), các path được đổi sang sub-resource chuẩn RESTful – xem §9.

### 4.3 PR-GĐ3: Review & Roll-up
**Diff files**:

1. `service/impl/SubmissionServiceImpl.java` (thêm `review`).
2. `service/impl/ProductionWorkflowServiceImpl.java` (bổ sung `recomputeTaskProgress`).
3. `model/Task.java` (thêm `progressPercentage`).
4. `model/ProductionPlan.java` (thêm `completionPercentage`).
5. `controller/SubmissionController.java` (thêm `/{id}/review`).
6. `controller/ProductionWorkflowController.java` (expose progress query nếu cần).

### 4.4 PR-GĐ4: Task-level submission & Cron
**Diff files**:

1. `controller/ProductionWorkflowController.java` (thêm `POST /tasks/{id}/submit`).
2. `service/impl/SubmissionServiceImpl.java` (cho phép `taskId` thay vì `subTaskId`).
3. `job/DeadlineReminderJob.java` (mới).
4. `MangaSystemAndManagementApplication.java` (`@EnableScheduling`).
5. `dto/request/CreateTaskSubmissionRequest.java` (mới, optional).

---

## 5. Caveats & Decisions cần xác nhận

### 5.1 Từ yêu cầu BA
1. **Rule cuối cùng ROUGH_SKETCH phải APPROVED trước khi nộp FINAL**: Đề xuất **enforce cứng** (throw `IllegalStateException` 400). Có thể cấu hình soft warning sau.
2. **Submission cấp Task**: Bài toán "gộp file từ các SubmissionFile final của SubTask, hoặc Mangaka upload file tổng hợp" — đề xuất **Mangaka upload file tổng hợp**, link tham chiếu trong response cho FE là đủ. Có thể cải tiến sau.
3. **`is_overdue`**: pure transient, không persist. Đúng yêu cầu BA.
4. **Reopen SubTask**: rule rằng "Task cha đã nộp lên Tantō → cảnh báo": đề xuất **soft warning** (trả về response với `warning: "..."`), không chặn cứng vì reopen hợp lệ khi Tantō chưa review.

### 5.2 Backward compatibility
- `Planning` (entity cũ) → vẫn dùng bởi `SubmissionServiceImpl.submitFiles()`. **Khuyến nghị**:
  - GĐ2: **deprecate** `POST /api/submissions/{userId}` cũ (đã có Notification cho client).
  - GĐ4: cân nhắc giữ nếu có FE cũ đang dùng — bằng cách giữ 1 controller proxy cho route cũ, hoặc redirect sang `/api/submissions`.
- `MangaWorkflowController` (Name submission + Board vote) — **không thuộc** scope này, giữ nguyên.
- `Feedback` (đã có): vẫn dùng cho Tantō feedback trên Task. Khi GĐ3, `Submission` sẽ có cơ chế approve/reject riêng cho Mangaka review Submission — **2 luồng song song**:
  - `Feedback.decision` (APPROVED/REJECTED) — Tantō feedback trên `Task` (legacy).
  - `Submission.status` (APPROVED/REJECTED) — Mangaka review trên `Submission` của SubTask/Task (mới).
  - Tránh nhầm: doc rõ trong Swagger 2 endpoint.

### 5.3 Migration order
- V2 + V3 đã chạy được. V4 chạy sau khi GĐ1 deploy.
- V4 có thay đổi cấu trúc Submission (drop cột Project/Planning trên Submission). **Cần đảm bảo V4 chạy trước khi service khởi động GĐ1** (vì Hibernate `validate` sẽ fail).

### 5.4 Dọn dẹp kỹ thuật nên làm trong các PR
- Bỏ `BootstrapAdminProperties.java` (đã xóa) — đã được xử lý.
- Cập nhật `SwaggerConfig` cho consistency (đã sửa một phần).
- Helper `getAccount(Long)` của `ProductionWorkflowServiceImpl` hiện dùng `AccountRepository.findById` — cân nhắc cache (không trong scope này).
- Comments giải thích tiếng Việt vẫn còn trong code → chuẩn hóa tiếng Anh.

---

## 6. Testing Strategy

| Lớp test | Mục tiêu | Tool |
|---|---|---|
| Unit | Validate entity methods (isOverdue, computeProgress), service logic happy + edge case | JUnit 5 + Mockito |
| Repository `@DataJpaTest` | Custom query methods (`findMaxVersion` v.v.) | @DataJpaTest + H2 |
| Integration `MockMvc` | API endpoint, RBAC, status code | Spring MockMvc |
| E2E (optional) | Full flow: Account → Plan → Task → SubTask → Submission → Review | TestRestTemplate + test SQL Server local |

Mỗi PR phải:
- Bao phủ **happy path** + **permission denied** + **deadline violation** + **race condition (version)**.

---

## 7. Risks & Rollback

| Risk | Mitigation |
|---|---|
| V4 drop cột gây mất data Submission cũ | Backup DB trước khi deploy; hoặc `Planning/ProjectId` nullable thay vì drop |
| Submission cũ trong DB trỏ Project (không Task/SubTask) | Hàm `getSubmittableType()` trong response trả `LEGACY` nếu cả 2 FK null |
| Concurrent submission của cùng SubTask | `@Version` column + optimistic lock trên Submission (Bổ sung nếu cần) |
| Cron chạy quá tải | Giới hạn query `WHERE deadline_date BETWEEN ... AND ...` + index |
| Race condition reopen SubTask | Validate version (SubTask.version) qua optimistic lock |

**Rollback**:
- V4 có thể rollback nếu chưa destroy code: chỉ cần quay lại commit trước.
- Service PRs: rollback về main, dữ liệu không mất (V4 giữ nguyên).

---

## 8. Open Questions cho PO

1. Có cần **giữ Submission cũ** (gắn Project/Planning) để Audit không? Nếu có → xem §5.2.
2. Final submission có BẮT BUỘC phải qua rough_sketch approved không, hay chỉ là khuyến nghị?
3. Cron reminder nhắn qua **Notification (in-app)** hay **Email**? Project có sẵn `Notification` + mail starter — đề xuất cả 2.
4. Reopen SubTask có cần **giới hạn số lần** reopen không?
5. Submission `submission_type` cho Task-level: dùng `TASK_LEVEL` hay một giá trị riêng (`CHAPTER_FINAL`?)?

---

## 9. RESTful naming audit (post-implementation)

Sau khi triển khai GĐ1–GĐ3, tiến hành audit REST chuẩn cho toàn bộ controller mới. Quy tắc:

| Quy tắc | Ví dụ |
|---|---|
| Resource là danh từ số nhiều | `/api/submissions`, `/api/tasks`, `/api/subtasks` |
| Path variable **luôn có tên cụ thể** | `{submissionId}`, `{taskId}`, `{subTaskId}`, `{planId}`, `{chapterId}`, `{projectId}`, `{accountId}`, `{userId}` |
| Hành động thể hiện qua HTTP method | `POST /api/submissions` (create), `PUT /api/chapters/{chapterId}/status` (update state) |
| Sub-resource được nest vào URL cha | `/api/tasks/{taskId}/subtasks`, `/api/subtasks/{subTaskId}/submissions` |
| Action trên 1 resource → sub-path verb-on-noun | `/api/submissions/{submissionId}/reviews`, `/api/subtasks/{subTaskId}/reopen` |

### 9.1 Bảng mapping cũ → mới

| Cũ | Mới | Lý do |
|---|---|---|
| `POST /api/submissions/upload` | `POST /api/submissions` | Bỏ action-verb `/upload`, dùng POST root |
| `GET /api/submissions/history?subTaskId=X` | `GET /api/subtasks/{subTaskId}/submissions` | Sub-resource – URL tự mô tả quan hệ |
| `GET /api/submissions/history?taskId=X` | `GET /api/tasks/{taskId}/submissions` | Sub-resource |
| `POST /api/submissions/{id}/review` | `POST /api/submissions/{submissionId}/reviews` | Đổi tên biến + `/reviews` (collection) |
| `GET /api/submissions/{id}` | `GET /api/submissions/{submissionId}` | Đổi tên biến |
| `DELETE /api/submissions/{id}` | `DELETE /api/submissions/{submissionId}` | Đổi tên biến |
| `POST /api/submissions/{userId}` (legacy) | (đã xóa) | Bỏ legacy, controller mới không cần |
| `POST /api/subtasks/tasks/{taskId}` | `POST /api/tasks/{taskId}/subtasks` | Sub-resource |
| `GET /api/subtasks?taskId=X&requesterId=Y` | `GET /api/tasks/{taskId}/subtasks?requesterId=Y` | Sub-resource |
| `GET /api/subtasks?assigneeId=X&requesterId=Y` | `GET /api/users/{userId}/subtasks?requesterId=Y` | Sub-resource (user-centric dashboard) |
| `GET /api/subtasks/{id}` | `GET /api/subtasks/{subTaskId}` | Đổi tên biến |
| `POST /api/subtasks/{id}/reopen` | `POST /api/subtasks/{subTaskId}/reopen` | Đổi tên biến |
| `GET /api/workflow/production-plans/{id}/dashboard` | `GET /api/workflow/production-plans/{planId}/dashboard` | Đổi tên biến |
| `POST /api/workflow/chapters/{id}/assign` | `POST /api/workflow/chapters/{chapterId}/assign` | Đổi tên biến |
| `PUT /api/workflow/chapters/{id}/status` | `PUT /api/workflow/chapters/{chapterId}/status` | Đổi tên biến |
| `PUT /api/workflow/tasks/{id}/status` | `PUT /api/workflow/tasks/{taskId}/status` | Đổi tên biến |
| `POST /api/workflow/tasks/{id}/feedback` | `POST /api/workflow/tasks/{taskId}/feedback` | Đổi tên biến |
| `POST /api/workflow/tasks/{id}/assign` | `POST /api/workflow/tasks/{taskId}/assign` | Đổi tên biến |
| `GET /api/workflow/projects/{id}/assets` | `GET /api/workflow/projects/{projectId}/assets` | Đổi tên biến |

### 9.2 Backward-compat

Vì project đang ở giai đoạn dev và frontend chưa chốt, ta **KHÔNG** giữ các path cũ. Nếu cần transition, có thể tạo 1 alias controller với `@Deprecated` mapping trỏ sang path mới trong 1 release.

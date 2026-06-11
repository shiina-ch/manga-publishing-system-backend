# Step 2 - Sketch Phase (Vẽ Phác Thảo) - API Documentation

## Implementation Summary

✅ **Complete implementation of Sketch Workflow for Manga Publishing System**

### What Was Created

#### 1. **Models (3 entities)**
- `SketchPage` - Main sketch page record with status tracking
- `SketchTask` - Individual task assigned to assistants (PERSPECTIVE, BACKGROUND, ENVIRONMENTAL_DETAILS)
- `SketchReview` - Tantor's review feedback with structured fields

#### 2. **Repositories (3 interfaces)**
- `SketchPageRepository` - Data access for sketch pages
- `SketchTaskRepository` - Data access for sketch tasks
- `SketchReviewRepository` - Data access for sketch reviews

#### 3. **DTOs (5 request classes)**
- `CreateSketchPageRequest` - Mangaka creates initial sketch
- `AssignSketchTaskRequest` - Distribute tasks to assistants
- `CompleteSketchTaskRequest` - Assistant completes their task
- `SubmitSketchReviewRequest` - Tantor submits review
- `RequestSketchChangesRequest` - Tantor requests revisions

#### 4. **Services (4 interfaces + 4 implementations)**
- `SketchPageService` - CRUD operations
- `SketchTaskService` - CRUD operations
- `SketchReviewService` - CRUD operations
- `SketchWorkflowService` - Workflow orchestration & state management

#### 5. **Controllers (4 REST controllers)**
- `SketchWorkflowController` - Primary workflow endpoints
- `SketchPageController` - CRUD REST API for sketch pages
- `SketchTaskController` - CRUD REST API for sketch tasks
- `SketchReviewController` - CRUD REST API for sketch reviews

---

## Workflow Status States

### SketchPage Status Flow
```
SKETCH_CREATED 
    ↓ (Mangaka assigns tasks)
TASKS_ASSIGNED 
    ↓ (All assistants complete tasks)
SKETCHES_COMPLETED 
    ↓ (Mangaka submits for review)
REVIEW_PENDING 
    ↓ (Tantor reviews)
APPROVED  OR  CHANGES_REQUESTED
```

### SketchTask Status Flow
```
ASSIGNED → IN_PROGRESS → COMPLETED
```

---

## API Endpoints

### Primary Workflow Endpoints: `/api/workflow/sketch`

#### 1. **Create Sketch Page**
```http
POST /api/workflow/sketch/create
Content-Type: application/json

{
  "chapterId": 1,
  "pageNumber": 1,
  "initialSketchUrl": "https://example.com/sketch1.png",
  "createdById": 2
}

Response (201):
{
  "statusCode": 201,
  "message": "Sketch page created",
  "data": {
    "id": 1,
    "chapter": { "id": 1, "chapterNumber": 1, "title": "Chapter 1" },
    "pageNumber": 1,
    "initialSketchUrl": "https://example.com/sketch1.png",
    "createdBy": { "id": 2, "email": "mangaka@example.com" },
    "status": "SKETCH_CREATED",
    "createdAt": "2026-06-11T16:10:00Z"
  }
}
```

**Authorization**: MANGAKA role required  
**Validation**: 
- Chapter must exist
- Creator must have MANGAKA role

---

#### 2. **Assign Tasks to Assistants**
```http
POST /api/workflow/sketch/assign-tasks
Content-Type: application/json

{
  "sketchPageId": 1,
  "tasks": [
    {
      "taskType": "PERSPECTIVE",
      "description": "Add perspective and depth to character positioning",
      "assignedToId": 3
    },
    {
      "taskType": "BACKGROUND",
      "description": "Draw background scenery and environment",
      "assignedToId": 4
    },
    {
      "taskType": "ENVIRONMENTAL_DETAILS",
      "description": "Add fine details like shadows and textures",
      "assignedToId": 5
    }
  ]
}

Response (200):
{
  "statusCode": 200,
  "message": "Tasks assigned",
  "data": null
}
```

**Authorization**: Any authenticated user (recommended: MANGAKA)  
**Auto-Update**: SketchPage status → `TASKS_ASSIGNED`  
**Validation**:
- SketchPage must exist
- All assignees must exist

---

#### 3. **Complete Sketch Task**
```http
POST /api/workflow/sketch/complete-task
Content-Type: application/json

{
  "sketchTaskId": 1,
  "completedUrl": "https://example.com/completed-perspective.png",
  "completedById": 3
}

Response (200):
{
  "statusCode": 200,
  "message": "Task completed",
  "data": {
    "id": 1,
    "sketchPage": { "id": 1, "status": "SKETCHES_COMPLETED" },
    "taskType": "PERSPECTIVE",
    "description": "Add perspective and depth to character positioning",
    "assignedTo": { "id": 3, "email": "assistant@example.com" },
    "completedUrl": "https://example.com/completed-perspective.png",
    "status": "COMPLETED",
    "completedAt": "2026-06-11T16:15:00Z"
  }
}
```

**Authorization**: Only assigned assistant can complete their task  
**Auto-Update**: 
- If all tasks completed → SketchPage status → `SKETCHES_COMPLETED`
- SketchTask status → `COMPLETED`

**Validation**:
- Task must exist
- Completer must be the assigned assistant
- Task not already completed

---

#### 4. **Submit Sketch for Review**
```http
POST /api/workflow/sketch/submit-review?sketchPageId=1&mangakaId=2

Response (200):
{
  "statusCode": 200,
  "message": "Sketch submitted for review",
  "data": null
}
```

**Authorization**: Only the original creator (Mangaka) can submit  
**Auto-Update**: SketchPage status → `REVIEW_PENDING`  
**Validation**:
- SketchPage must exist
- All tasks must be COMPLETED
- Submitter must be the original creator
- SketchPage must be in SKETCHES_COMPLETED status

---

#### 5. **Tantor Reviews Sketch**
```http
POST /api/workflow/sketch/review
Content-Type: application/json

{
  "sketchPageId": 1,
  "reviewerId": 6,
  "decision": "APPROVE",
  "comment": "Excellent work on the layout and character placement.",
  "layoutFeedback": "Perspective is well done, composition is balanced.",
  "detailsFeedback": "Fine details need more shading in shadows."
}

Response (200):
{
  "statusCode": 200,
  "message": "Review submitted",
  "data": {
    "id": 1,
    "sketchPage": { "id": 1 },
    "reviewer": { "id": 6, "email": "tantor@example.com" },
    "decision": "APPROVE",
    "comment": "Excellent work on the layout and character placement.",
    "layoutFeedback": "Perspective is well done, composition is balanced.",
    "detailsFeedback": "Fine details need more shading in shadows.",
    "reviewedAt": "2026-06-11T16:20:00Z"
  }
}
```

**Authorization**: TANTOR role required  
**Auto-Update**: Based on decision:
- `APPROVE` or `APPROVED` → SketchPage status → `APPROVED`
- `REQUEST_CHANGES` or `CHANGES_REQUESTED` → SketchPage status → `CHANGES_REQUESTED`

**Validation**:
- SketchPage must exist and be in REVIEW_PENDING status
- Reviewer must be TANTOR

---

#### 6. **Request Sketch Changes**
```http
POST /api/workflow/sketch/request-changes?sketchPageId=1&reviewerId=6&comment=Please%20revise%20the%20background

Response (200):
{
  "statusCode": 200,
  "message": "Changes requested",
  "data": null
}
```

**Authorization**: TANTOR role required  
**Auto-Update**: 
- SketchPage status → `CHANGES_REQUESTED`
- Creates SketchReview record for tracking

**Validation**:
- SketchPage must exist
- Reviewer must be TANTOR

---

#### 7. **Get Sketch Page Status**
```http
GET /api/workflow/sketch/status/1

Response (200):
{
  "statusCode": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "chapter": { "id": 1, "chapterNumber": 1, "title": "Chapter 1" },
    "pageNumber": 1,
    "initialSketchUrl": "https://example.com/sketch1.png",
    "createdBy": { "id": 2, "email": "mangaka@example.com" },
    "status": "APPROVED",
    "createdAt": "2026-06-11T16:10:00Z",
    "updatedAt": "2026-06-11T16:20:00Z"
  }
}
```

---

### CRUD Endpoints

#### Sketch Pages CRUD: `/api/sketch-pages`
```http
POST   /api/sketch-pages              # Create
GET    /api/sketch-pages              # List all
GET    /api/sketch-pages/{id}         # Get one
PUT    /api/sketch-pages/{id}         # Update
DELETE /api/sketch-pages/{id}         # Delete
```

#### Sketch Tasks CRUD: `/api/sketch-tasks`
```http
POST   /api/sketch-tasks              # Create
GET    /api/sketch-tasks              # List all
GET    /api/sketch-tasks/{id}         # Get one
PUT    /api/sketch-tasks/{id}         # Update
DELETE /api/sketch-tasks/{id}         # Delete
```

#### Sketch Reviews CRUD: `/api/sketch-reviews`
```http
POST   /api/sketch-reviews            # Create
GET    /api/sketch-reviews            # List all
GET    /api/sketch-reviews/{id}       # Get one
PUT    /api/sketch-reviews/{id}       # Update
DELETE /api/sketch-reviews/{id}       # Delete
```

---

## Role-Based Access Control

| Role | Permissions |
|------|---|
| **MANGAKA** | Create sketch page, assign tasks, submit for review |
| **Assistant** (Project Member) | Complete assigned tasks |
| **TANTOR** | Review sketches, approve, request changes |
| **ADMIN** | Full access to all operations |

---

## Error Handling

| Status Code | Scenario | Example |
|-------------|----------|---------|
| **201** | Resource created successfully | Sketch page created |
| **200** | Operation successful | Task completed, review submitted |
| **400** | Bad request / validation error | Missing required field, invalid state transition |
| **403** | Forbidden / insufficient permissions | Non-MANGAKA trying to create sketch page |
| **404** | Resource not found | Referenced chapter doesn't exist |
| **409** | Conflict / business logic violation | Cannot submit if tasks not completed |
| **500** | Server error | Unexpected exception |

---

## Database Schema Notes

**Tables Created:**
- `SketchPage` - References `Chapter` and `Account`
- `SketchTask` - References `SketchPage` and `Account`
- `SketchReview` - References `SketchPage` and `Account`

**Key Constraints:**
- Foreign keys enforce referential integrity
- Timestamps use `java.time.Instant` with DB default `getdate()`
- Status fields are VARCHAR(50) strings
- Cascade delete configured for SketchPage → SketchTask

---

## Integration with Existing System

✅ **Chapter Entity Updated**
- Added `@OneToMany` relationship to `List<SketchPage>`
- Cascade delete enabled for data consistency

✅ **Follows Existing Patterns**
- 3-layer architecture (Controller → Service → Repository)
- ResponseBase wrapper for all responses
- Lombok @Getter/@Setter for POJOs
- @Transactional for data consistency
- Exception handling with proper HTTP status codes

✅ **Compilation Status**
- ✅ All 20+ new classes compile successfully
- ✅ Build successful: `BUILD SUCCESS`

---

## Usage Examples

### Example 1: Complete Workflow
1. **Mangaka creates sketch page**
   ```
   POST /api/workflow/sketch/create
   ```

2. **Mangaka assigns tasks to 3 assistants**
   ```
   POST /api/workflow/sketch/assign-tasks
   ```

3. **Each assistant completes their task**
   ```
   POST /api/workflow/sketch/complete-task (3 times)
   ```

4. **Mangaka submits for review**
   ```
   POST /api/workflow/sketch/submit-review?sketchPageId=1&mangakaId=2
   ```

5. **Tantor reviews and approves**
   ```
   POST /api/workflow/sketch/review (decision: APPROVE)
   ```

### Example 2: Workflow with Revisions
1. Steps 1-4 as above
2. **Tantor requests changes**
   ```
   POST /api/workflow/sketch/request-changes?sketchPageId=1&reviewerId=6
   ```

3. **Mangaka and assistants revise** (reassign tasks manually)
4. **Submit again for review**
5. **Tantor approves**

---

## Next Steps (Future Enhancements)

- [ ] File upload service integration
- [ ] Skill-based automatic task assignment
- [ ] Progress tracking & analytics
- [ ] WebSocket notifications for real-time updates
- [ ] Bulk operations & batch processing
- [ ] Advanced feedback with annotations/markup
- [ ] Version control for sketches
- [ ] Approval workflow with multiple reviewers


package group1.com.MangaSystemAndManagement.service.interfaces;

import group1.com.MangaSystemAndManagement.dto.request.*;
import group1.com.MangaSystemAndManagement.dto.response.*;

import java.util.List;

public interface ProductionWorkflowService {

    ProjectResponse createProject(CreateProjectRequest req, Long creatorId);

    ProjectResponse activateProject(Long projectId, Long requesterId);

    PlanDashboardResponse getPlanDashboard(Long planId, Long requesterId);

    ChapterWithTasksResponse createChapter(CreateChapterRequest req, Long requesterId);

    ChapterWithTasksResponse updateChapterStatus(Long chapterId, group1.com.MangaSystemAndManagement.model.ChapterStatus status, Long requesterId);

    ChapterResponse assignChapter(Long chapterId, AssignChapterRequest req);

    TaskResponse updateTaskStatus(Long taskId, UpdateTaskStatusRequest req);

    FeedbackResponse createFeedback(Long taskId, CreateFeedbackRequest req);

    TaskResponse assignTask(Long taskId, AssignTaskRequest req);

    List<AssetResponse> getProjectAssets(Long projectId, Long requesterId);

}

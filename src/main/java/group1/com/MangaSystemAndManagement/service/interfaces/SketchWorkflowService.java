package group1.com.MangaSystemAndManagement.service.interfaces;

import group1.com.MangaSystemAndManagement.dto.request.AssignSketchTaskRequest;
import group1.com.MangaSystemAndManagement.dto.request.CompleteSketchTaskRequest;
import group1.com.MangaSystemAndManagement.dto.request.CreateSketchPageRequest;
import group1.com.MangaSystemAndManagement.dto.request.SubmitSketchReviewRequest;
import group1.com.MangaSystemAndManagement.model.SketchPage;
import group1.com.MangaSystemAndManagement.model.SketchReview;
import group1.com.MangaSystemAndManagement.model.SketchTask;

public interface SketchWorkflowService {
    SketchPage createSketchPage(CreateSketchPageRequest req);
    void assignTasksToAssistants(AssignSketchTaskRequest req);
    SketchTask completeSketchTask(CompleteSketchTaskRequest req);
    void submitSketchForReview(Long sketchPageId, Long mangakaId);
    SketchReview reviewSketch(SubmitSketchReviewRequest req);
    void requestSketchChanges(Long sketchPageId, Long reviewerId, String comment);
    SketchPage getSketchPageStatus(Long sketchPageId);
}

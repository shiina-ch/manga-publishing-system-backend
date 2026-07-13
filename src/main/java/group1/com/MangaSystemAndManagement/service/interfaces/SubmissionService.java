package group1.com.MangaSystemAndManagement.service.interfaces;

import group1.com.MangaSystemAndManagement.dto.request.CreateSubTaskSubmissionRequest;
import group1.com.MangaSystemAndManagement.dto.request.CreateTaskSubmissionRequest;
import group1.com.MangaSystemAndManagement.dto.request.CreateSubmissionRequest;
import group1.com.MangaSystemAndManagement.dto.request.ReviewSubmissionRequest;
import group1.com.MangaSystemAndManagement.dto.request.SubmissionRequest;
import group1.com.MangaSystemAndManagement.dto.response.SubmissionResponse;
import group1.com.MangaSystemAndManagement.model.Submission;

import java.util.List;
import java.util.Optional;

public interface SubmissionService {

    @Deprecated
    SubmissionResponse create(CreateSubmissionRequest req);

    SubmissionResponse createForSubTask(Long subTaskId, CreateSubTaskSubmissionRequest req);

    SubmissionResponse createForTask(Long taskId, CreateTaskSubmissionRequest req);

    SubmissionResponse review(Long submissionId, ReviewSubmissionRequest req);

    List<SubmissionResponse> historyBySubTask(Long subTaskId);

    List<SubmissionResponse> historyByTask(Long taskId);

    Optional<Submission> findById(Long id);

    Submission findByIdOrThrow(Long id);

    List<Submission> findAll();

    @Deprecated
    Submission submitFiles(Long accountId, SubmissionRequest request);

    @Deprecated
    Submission update(Long id, SubmissionRequest request);

    @Deprecated
    void delete(Long id);

    /**
     * Approve a legacy submission and auto-create a Project from its data.
     * Used by the editorial-board approve workflow (POST /api/submissions/{id}/approve).
     */
    Submission approveAndCreateProject(Long submissionId);
}

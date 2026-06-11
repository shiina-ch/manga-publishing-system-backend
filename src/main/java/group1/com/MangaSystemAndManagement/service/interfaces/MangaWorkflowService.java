package group1.com.MangaSystemAndManagement.service.interfaces;

import group1.com.MangaSystemAndManagement.dto.request.NameSubmissionRequest;
import group1.com.MangaSystemAndManagement.dto.request.ReviewRequest;
import group1.com.MangaSystemAndManagement.dto.request.ResubmitRequest;
import group1.com.MangaSystemAndManagement.model.Submission;
import group1.com.MangaSystemAndManagement.model.SubmissionReview;

import java.util.List;

public interface MangaWorkflowService {
    Submission submitName(NameSubmissionRequest req);
    SubmissionReview reviewName(ReviewRequest req);
    Submission resubmitName(ResubmitRequest req);
    List<Submission> listSubmissions(String status);
    List<SubmissionReview> listReviewsForSubmission(Long submissionId);
}

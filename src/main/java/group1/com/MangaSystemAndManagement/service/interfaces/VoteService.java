package group1.com.MangaSystemAndManagement.service.interfaces;
import group1.com.MangaSystemAndManagement.dto.request.VoteRequest;
import group1.com.MangaSystemAndManagement.dto.response.VoteResponse;
import group1.com.MangaSystemAndManagement.dto.response.VoteSummaryResponse;

import java.util.List;

public interface VoteService {
    VoteResponse create(VoteRequest request);
    VoteResponse findById(Long id);
    List<VoteResponse> findAll();
    VoteResponse update(Long id, VoteRequest request);
    void delete(Long id, Long voterId);
    List<VoteResponse> findBySubmissionReviewId(Long submissionReviewId);
    VoteSummaryResponse getSummary(Long submissionReviewId);
}

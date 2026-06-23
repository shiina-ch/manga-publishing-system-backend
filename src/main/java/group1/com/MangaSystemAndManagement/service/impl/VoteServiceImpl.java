package group1.com.MangaSystemAndManagement.service.impl;
import group1.com.MangaSystemAndManagement.dto.request.VoteRequest;
import group1.com.MangaSystemAndManagement.dto.response.VoteResponse;
import group1.com.MangaSystemAndManagement.dto.response.VoteSummaryResponse;
import group1.com.MangaSystemAndManagement.exception.EntityNotFoundException;
import group1.com.MangaSystemAndManagement.model.Account;
import group1.com.MangaSystemAndManagement.model.SubmissionReview;
import group1.com.MangaSystemAndManagement.model.Vote;
import group1.com.MangaSystemAndManagement.model.VoteResult;
import group1.com.MangaSystemAndManagement.model.VoteValue;
import group1.com.MangaSystemAndManagement.repository.AccountRepository;
import group1.com.MangaSystemAndManagement.repository.SubmissionReviewRepository;
import group1.com.MangaSystemAndManagement.repository.VoteRepository;
import group1.com.MangaSystemAndManagement.service.interfaces.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {
    private final VoteRepository voteRepository;
    private final AccountRepository accountRepository;
    private final SubmissionReviewRepository submissionReviewRepository;

    @Override
    @Transactional
    public VoteResponse create(VoteRequest request) {
        validateRequest(request);
        SubmissionReview submissionReview = findSubmissionReview(request.getSubmissionReviewId());
        Account voter = findVoter(request.getVoterId());
        validateVotingPermission(voter);

        Vote vote = voteRepository
                .findBySubmissionReview_IdAndVoter_Id(request.getSubmissionReviewId(), request.getVoterId())
                .orElseGet(() -> {
                    Vote newVote = new Vote();
                    newVote.setSubmissionReview(submissionReview);
                    newVote.setVoter(voter);
                    return newVote;
                });

        applyVoteChanges(vote, request);
        return toResponse(voteRepository.save(vote));
    }

    @Override
    @Transactional(readOnly = true)
    public VoteResponse findById(Long id) {
        return toResponse(findVote(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoteResponse> findAll() {
        return voteRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public VoteResponse update(Long id, VoteRequest request) {
        validateRequest(request);
        Vote vote = findVote(id);

        if (!Objects.equals(vote.getVoter().getId(), request.getVoterId())) {
            throw new AccessDeniedException("A voter may only update their own vote");
        }
        if (!vote.getSubmissionReview().getId().equals(request.getSubmissionReviewId())) {
            throw new IllegalArgumentException("A vote cannot be moved to another SubmissionReview");
        }

        Account voter = findVoter(request.getVoterId());
        validateVotingPermission(voter);
        applyVoteChanges(vote, request);
        return toResponse(voteRepository.save(vote));
    }

    @Override
    @Transactional
    public void delete(Long id, Long voterId) {
        if (voterId == null) {
            throw new IllegalArgumentException("voterId is required");
        }
        Vote vote = findVote(id);
        if (!Objects.equals(vote.getVoter().getId(), voterId)) {
            throw new AccessDeniedException("A voter may only delete their own vote");
        }

        Account voter = findVoter(voterId);
        validateVotingPermission(voter);
        voteRepository.delete(vote);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoteResponse> findBySubmissionReviewId(Long submissionReviewId) {
        findSubmissionReview(submissionReviewId);
        return voteRepository.findAllBySubmissionReview_Id(submissionReviewId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VoteSummaryResponse getSummary(Long submissionReviewId) {
        findSubmissionReview(submissionReviewId);
        long approveCount = voteRepository.countBySubmissionReview_IdAndVoteValue(
                submissionReviewId, VoteValue.APPROVE);
        long rejectCount = voteRepository.countBySubmissionReview_IdAndVoteValue(
                submissionReviewId, VoteValue.REJECT);
        VoteResult result = approveCount > rejectCount ? VoteResult.APPROVED : VoteResult.REJECTED;
        return new VoteSummaryResponse(
                submissionReviewId,
                approveCount,
                rejectCount,
                approveCount + rejectCount,
                result
        );
    }

    private Vote findVote(Long id) {
        return voteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vote not found with id " + id));
    }

    private SubmissionReview findSubmissionReview(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("submissionReviewId is required");
        }
        return submissionReviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SubmissionReview not found with id " + id));
    }

    private Account findVoter(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("voterId is required");
        }
        return accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with id " + id));
    }

    private void validateRequest(VoteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Vote request is required");
        }
        if (request.getSubmissionReviewId() == null) {
            throw new IllegalArgumentException("submissionReviewId is required");
        }
        if (request.getVoterId() == null) {
            throw new IllegalArgumentException("voterId is required");
        }
        if (request.getVoteValue() == null) {
            throw new IllegalArgumentException("voteValue must be APPROVE or REJECT");
        }
        if (request.getVoteValue() == VoteValue.REJECT
                && (request.getComment() == null || request.getComment().isBlank())) {
            throw new IllegalArgumentException("A comment is required when rejecting a SubmissionReview");
        }
    }

    private void validateVotingPermission(Account voter) {
        if (!"ACTIVE".equalsIgnoreCase(voter.getStatus())) {
            throw new AccessDeniedException("Only ACTIVE accounts may vote");
        }
        boolean isEditor = voter.getSystemRole() != null && voter.getSystemRole().stream()
                .anyMatch(role -> "EDITOR".equalsIgnoreCase(role.getRoleName()));
        if (!isEditor) {
            throw new AccessDeniedException("Only accounts with the EDITOR role may vote");
        }
    }

    private void applyVoteChanges(Vote vote, VoteRequest request) {
        vote.setVoteValue(request.getVoteValue());
        vote.setComment(normalizeComment(request));
        vote.setVotedAt(Instant.now());
    }

    private String normalizeComment(VoteRequest request) {
        if (request.getComment() == null || request.getComment().isBlank()) {
            return null;
        }
        return request.getComment().trim();
    }

    private VoteResponse toResponse(Vote vote) {
        return new VoteResponse(
                vote.getId(),
                vote.getSubmissionReview().getId(),
                vote.getVoter().getId(),
                vote.getVoteValue(),
                vote.getComment(),
                vote.getVotedAt()
        );
    }
}

package group1.com.MangaSystemAndManagement.service;

import group1.com.MangaSystemAndManagement.dto.response.VoteSummaryResponse;
import group1.com.MangaSystemAndManagement.model.Account;
import group1.com.MangaSystemAndManagement.model.SubmissionReview;
import group1.com.MangaSystemAndManagement.model.SystemRole;
import group1.com.MangaSystemAndManagement.repository.AccountRepository;
import group1.com.MangaSystemAndManagement.repository.SubmissionReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoteServiceImplTest {

    private static final long REVIEW_ID = 10L;
    private static final long VOTER_ID = 1000L;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private SubmissionReviewRepository submissionReviewRepository;

    private VoteServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new VoteServiceImpl(voteRepository, accountRepository, submissionReviewRepository);
    }

    @Test
    void createsApproveWithoutComment() {
        stubEligibleVoteCreation(Optional.empty());

        VoteResponse response = service.create(request(VOTER_ID, VoteValue.APPROVE, null));

        assertEquals(VoteValue.APPROVE, response.getVoteValue());
        assertNull(response.getComment());
        verify(voteRepository).save(any(Vote.class));
    }

    @Test
    void createsRejectWithComment() {
        stubEligibleVoteCreation(Optional.empty());

        VoteResponse response = service.create(request(VOTER_ID, VoteValue.REJECT, "  insufficient quality  "));

        assertEquals(VoteValue.REJECT, response.getVoteValue());
        assertEquals("insufficient quality", response.getComment());
    }

    @Test
    void rejectsRejectWithoutComment() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(request(VOTER_ID, VoteValue.REJECT, "  "))
        );

        assertTrue(exception.getMessage().contains("comment"));
        verify(voteRepository, never()).save(any());
    }

    @Test
    void rejectsNonEditorVoter() {
        SubmissionReview review = review();
        Account voter = voter(VOTER_ID, "ACTIVE", "MANGAKA");
        when(submissionReviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));
        when(accountRepository.findById(VOTER_ID)).thenReturn(Optional.of(voter));

        assertThrows(
                AccessDeniedException.class,
                () -> service.create(request(VOTER_ID, VoteValue.APPROVE, null))
        );
        verify(voteRepository, never()).save(any());
    }

    @Test
    void rejectsInactiveVoter() {
        SubmissionReview review = review();
        Account voter = voter(VOTER_ID, "INACTIVE", "EDITORIAL_BOARD_MEMBER");
        when(submissionReviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));
        when(accountRepository.findById(VOTER_ID)).thenReturn(Optional.of(voter));

        assertThrows(
                AccessDeniedException.class,
                () -> service.create(request(VOTER_ID, VoteValue.APPROVE, null))
        );
        verify(voteRepository, never()).save(any());
    }

    @Test
    void assignsVotedAtInBackend() {
        stubEligibleVoteCreation(Optional.empty());
        Instant before = Instant.now();

        VoteResponse response = service.create(request(VOTER_ID, VoteValue.APPROVE, null));
        Instant after = Instant.now();

        assertNotNull(response.getVotedAt());
        assertFalse(response.getVotedAt().isBefore(before));
        assertFalse(response.getVotedAt().isAfter(after));
    }

    @Test
    void repeatedPostUpdatesExistingVoteInsteadOfInserting() {
        Vote existing = vote(99L, voter(VOTER_ID, "ACTIVE", "EDITORIAL_BOARD_MEMBER"), VoteValue.REJECT, "old reason");
        Instant previousTime = Instant.parse("2024-01-01T00:00:00Z");
        existing.setVotedAt(previousTime);
        stubEligibleVoteCreation(Optional.of(existing));

        VoteResponse response = service.create(request(VOTER_ID, VoteValue.APPROVE, " "));

        assertEquals(99L, response.getId());
        assertEquals(VoteValue.APPROVE, existing.getVoteValue());
        assertNull(existing.getComment());
        assertTrue(existing.getVotedAt().isAfter(previousTime));
        verify(voteRepository).save(existing);
    }

    @Test
    void putCannotChangeVoteOwner() {
        Vote existing = vote(99L, voter(VOTER_ID, "ACTIVE", "EDITORIAL_BOARD_MEMBER"), VoteValue.APPROVE, null);
        when(voteRepository.findById(99L)).thenReturn(Optional.of(existing));

        assertThrows(
                AccessDeniedException.class,
                () -> service.update(99L, request(1001L, VoteValue.REJECT, "reason"))
        );
        verify(voteRepository, never()).save(any());
    }

    @Test
    void putAcceptsSeparatelyBoxedEqualVoterId() {
        Account voter = voter(VOTER_ID, "ACTIVE", "EDITORIAL_BOARD_MEMBER");
        Vote existing = vote(99L, voter, VoteValue.APPROVE, null);
        Long requestVoterId = Long.valueOf("1000");
        Long separatelyBoxedStoredId = Long.valueOf(voter.getId());
        assertNotSame(separatelyBoxedStoredId, requestVoterId);
        when(voteRepository.findById(99L)).thenReturn(Optional.of(existing));
        when(accountRepository.findById(requestVoterId)).thenReturn(Optional.of(voter));
        when(voteRepository.save(existing)).thenReturn(existing);

        VoteResponse response = service.update(
                99L,
                new VoteRequest(REVIEW_ID, requestVoterId, VoteValue.REJECT, "reason")
        );

        assertEquals(VoteValue.REJECT, response.getVoteValue());
        verify(voteRepository).save(existing);
    }

    @Test
    void anotherVoterCannotDeleteVote() {
        Vote existing = vote(99L, voter(VOTER_ID, "ACTIVE", "EDITORIAL_BOARD_MEMBER"), VoteValue.APPROVE, null);
        when(voteRepository.findById(99L)).thenReturn(Optional.of(existing));

        assertThrows(AccessDeniedException.class, () -> service.delete(99L, 1001L));
        verify(voteRepository, never()).delete(any());
    }

    @Test
    void deleteAcceptsSeparatelyBoxedEqualVoterId() {
        Account voter = voter(VOTER_ID, "ACTIVE", "EDITORIAL_BOARD_MEMBER");
        Vote existing = vote(99L, voter, VoteValue.APPROVE, null);
        Long requestVoterId = Long.valueOf("1000");
        Long separatelyBoxedStoredId = Long.valueOf(voter.getId());
        assertNotSame(separatelyBoxedStoredId, requestVoterId);
        when(voteRepository.findById(99L)).thenReturn(Optional.of(existing));
        when(accountRepository.findById(requestVoterId)).thenReturn(Optional.of(voter));

        service.delete(99L, requestVoterId);

        verify(voteRepository).delete(existing);
    }

    @Test
    void approveMajorityReturnsApproved() {
        VoteSummaryResponse summary = summary(3, 2);

        assertEquals(VoteResult.APPROVED, summary.getResult());
        assertEquals(5, summary.getTotalVotes());
    }

    @Test
    void rejectMajorityReturnsRejected() {
        VoteSummaryResponse summary = summary(1, 2);

        assertEquals(VoteResult.REJECTED, summary.getResult());
    }

    @Test
    void tieReturnsRejected() {
        VoteSummaryResponse summary = summary(2, 2);

        assertEquals(VoteResult.REJECTED, summary.getResult());
    }

    @Test
    void zeroVotesReturnsRejected() {
        VoteSummaryResponse summary = summary(0, 0);

        assertEquals(VoteResult.REJECTED, summary.getResult());
        assertEquals(0, summary.getTotalVotes());
    }

    private VoteSummaryResponse summary(long approveCount, long rejectCount) {
        when(submissionReviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review()));
        when(voteRepository.countBySubmissionReview_IdAndVoteValue(REVIEW_ID, VoteValue.APPROVE))
                .thenReturn(approveCount);
        when(voteRepository.countBySubmissionReview_IdAndVoteValue(REVIEW_ID, VoteValue.REJECT))
                .thenReturn(rejectCount);
        return service.getSummary(REVIEW_ID);
    }

    private void stubEligibleVoteCreation(Optional<Vote> existingVote) {
        SubmissionReview review = review();
        Account voter = existingVote.map(Vote::getVoter)
                .orElseGet(() -> voter(VOTER_ID, "ACTIVE", "EDITORIAL_BOARD_MEMBER"));
        when(submissionReviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));
        when(accountRepository.findById(VOTER_ID)).thenReturn(Optional.of(voter));
        when(voteRepository.findBySubmissionReview_IdAndVoter_Id(REVIEW_ID, VOTER_ID))
                .thenReturn(existingVote);
        when(voteRepository.save(any(Vote.class))).thenAnswer(invocation -> {
            Vote saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(1L);
            }
            return saved;
        });
    }

    private VoteRequest request(long voterId, VoteValue voteValue, String comment) {
        return new VoteRequest(REVIEW_ID, voterId, voteValue, comment);
    }

    private SubmissionReview review() {
        SubmissionReview review = new SubmissionReview();
        review.setId(REVIEW_ID);
        return review;
    }

    private Account voter(long id, String status, String roleName) {
        SystemRole role = new SystemRole();
        role.setRoleName(roleName);
        Account account = new Account();
        account.setId(id);
        account.setStatus(status);
        account.setSystemRole(List.of(role));
        return account;
    }

    private Vote vote(long id, Account voter, VoteValue voteValue, String comment) {
        Vote vote = new Vote();
        vote.setId(id);
        vote.setSubmissionReview(review());
        vote.setVoter(voter);
        vote.setVoteValue(voteValue);
        vote.setComment(comment);
        vote.setVotedAt(Instant.now());
        return vote;
    }
}

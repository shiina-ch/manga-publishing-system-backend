package group1.com.MangaSystemAndManagement.repository;
import group1.com.MangaSystemAndManagement.model.Vote;
import group1.com.MangaSystemAndManagement.model.VoteValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findBySubmissionReview_IdAndVoter_Id(Long submissionReviewId, Long voterId);

    List<Vote> findAllBySubmissionReview_Id(Long submissionReviewId);

    long countBySubmissionReview_IdAndVoteValue(Long submissionReviewId, VoteValue voteValue);
}

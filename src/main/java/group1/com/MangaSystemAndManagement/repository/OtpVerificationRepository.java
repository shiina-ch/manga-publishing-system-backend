package group1.com.MangaSystemAndManagement.repository;

import group1.com.MangaSystemAndManagement.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByEmailAndIsUsedFalseOrderByExpiredAtDesc(String email);
}

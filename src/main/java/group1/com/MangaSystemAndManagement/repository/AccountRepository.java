package group1.com.MangaSystemAndManagement.repository;

import group1.com.MangaSystemAndManagement.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByEmailIgnoreCase(String email);

    List<Account> findByRequestedRoleInOrderByIdDesc(Collection<String> requestedRoles);

    List<Account> findByStatusIgnoreCaseAndRequestedRoleInOrderByIdDesc(
            String status, Collection<String> requestedRoles);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdForUpdate(Long id);
}

package group1.com.MangaSystemAndManagement.service.impl;

import group1.com.MangaSystemAndManagement.model.Account;
import group1.com.MangaSystemAndManagement.model.SystemRole;
import group1.com.MangaSystemAndManagement.model.SystemRoleName;
import group1.com.MangaSystemAndManagement.repository.AccountRepository;
import group1.com.MangaSystemAndManagement.repository.SystemRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemRoleNormalizationService {

    private final SystemRoleRepository systemRoleRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public void normalizeLegacyRoles() {
        List<SystemRole> allRoles = new ArrayList<>(systemRoleRepository.findAll());
        List<Account> allAccounts = new ArrayList<>(accountRepository.findAll());

        for (SystemRoleName canonicalName : SystemRoleName.values()) {
            List<SystemRole> equivalentRoles = allRoles.stream()
                    .filter(role -> canonicalName.matches(role.getRoleName()))
                    .toList();
            if (equivalentRoles.isEmpty()) {
                continue;
            }

            SystemRole survivor = selectSurvivor(canonicalName, equivalentRoles);

            if (!canonicalName.name().equals(survivor.getRoleName())) {
                survivor.setRoleName(canonicalName.name());
                systemRoleRepository.save(survivor);
                log.info("Normalized system role to {}", canonicalName);
            }

            normalizeAccountRelationships(canonicalName, survivor, allAccounts);
            deleteDuplicateRoleRows(survivor, equivalentRoles, allAccounts);
        }
    }

    private SystemRole selectSurvivor(SystemRoleName canonicalName, List<SystemRole> equivalentRoles) {
        Comparator<SystemRole> comparator = Comparator
                .comparing((SystemRole role) -> !canonicalName.name().equals(role.getRoleName()))
                .thenComparing(role -> role.getId() <= 0 ? Long.MAX_VALUE : role.getId())
                .thenComparing(role -> role.getRoleName() == null ? "" : role.getRoleName())
                .thenComparingInt(equivalentRoles::indexOf);
        return equivalentRoles.stream().min(comparator).orElseThrow();
    }

    private void normalizeAccountRelationships(
            SystemRoleName canonicalName,
            SystemRole survivor,
            List<Account> accounts) {
        for (Account account : accounts) {
            if (replaceEquivalentRoles(account, canonicalName, survivor)) {
                accountRepository.save(account);
            }
        }
    }

    private void deleteDuplicateRoleRows(
            SystemRole survivor,
            List<SystemRole> equivalentRoles,
            List<Account> accounts) {
        List<SystemRole> duplicates = equivalentRoles.stream()
                .filter(role -> !samePersistedRole(role, survivor))
                .toList();
        if (duplicates.isEmpty()) {
            return;
        }

        boolean stillReferenced = accounts.stream()
                .flatMap(account -> account.getSystemRole() == null
                        ? java.util.stream.Stream.empty()
                        : account.getSystemRole().stream())
                .anyMatch(role -> duplicates.stream().anyMatch(duplicate -> samePersistedRole(role, duplicate)));
        if (stillReferenced) {
            throw new IllegalStateException("Cannot delete a legacy system role while accounts still reference it");
        }

        accountRepository.flush();
        duplicates.forEach(systemRoleRepository::delete);
    }

    private boolean replaceEquivalentRoles(
            Account account,
            SystemRoleName canonicalName,
            SystemRole survivor) {
        List<SystemRole> currentRoles = account.getSystemRole();
        if (currentRoles == null || currentRoles.stream()
                .filter(java.util.Objects::nonNull)
                .noneMatch(role -> canonicalName.matches(role.getRoleName()))) {
            return false;
        }

        List<SystemRole> normalizedRoles = new ArrayList<>();
        boolean survivorAdded = false;
        boolean changed = false;
        for (SystemRole role : currentRoles) {
            if (role != null && canonicalName.matches(role.getRoleName())) {
                if (!survivorAdded) {
                    normalizedRoles.add(survivor);
                    survivorAdded = true;
                    changed = !samePersistedRole(role, survivor);
                } else {
                    changed = true;
                }
            } else if (!normalizedRoles.contains(role)) {
                normalizedRoles.add(role);
            } else {
                changed = true;
            }
        }
        if (!changed && normalizedRoles.size() == currentRoles.size()) {
            return false;
        }
        account.setSystemRole(normalizedRoles);
        return true;
    }

    private boolean samePersistedRole(SystemRole first, SystemRole second) {
        if (first == second) {
            return true;
        }
        return first != null && second != null
                && first.getId() > 0
                && first.getId() == second.getId();
    }
}

package group1.com.MangaSystemAndManagement.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import group1.com.MangaSystemAndManagement.dto.request.AccountLoginRequest;
import group1.com.MangaSystemAndManagement.dto.request.AccountRequest;
import group1.com.MangaSystemAndManagement.dto.response.AccountResponse;
import group1.com.MangaSystemAndManagement.exception.AccountStateConflictException;
import group1.com.MangaSystemAndManagement.exception.AccountStatusException;
import group1.com.MangaSystemAndManagement.exception.DuplicateEmailException;
import group1.com.MangaSystemAndManagement.exception.InvalidPublicRoleException;
import group1.com.MangaSystemAndManagement.exception.ResourceNotFoundException;
import group1.com.MangaSystemAndManagement.model.Account;
import group1.com.MangaSystemAndManagement.model.AccountStatus;
import group1.com.MangaSystemAndManagement.model.SystemRole;
import group1.com.MangaSystemAndManagement.model.SystemRoleName;
import group1.com.MangaSystemAndManagement.repository.AccountRepository;
import group1.com.MangaSystemAndManagement.repository.SystemRoleRepository;
import group1.com.MangaSystemAndManagement.security.service.AuthenticationService;
import group1.com.MangaSystemAndManagement.service.interfaces.AccountService;
import group1.com.MangaSystemAndManagement.service.interfaces.NotificationService;

@Service
public class AccountServiceImpl implements AccountService {

    private static final Set<SystemRoleName> PUBLIC_ROLES = Set.of(
            SystemRoleName.MANAGER, SystemRoleName.TANTOU_EDITOR,
            SystemRoleName.EDITORIAL_BOARD_MEMBER, SystemRoleName.MANGAKA, SystemRoleName.ASSISTANT);
    private static final Set<SystemRoleName> MANAGER_SCOPED_ROLES = Set.of(
            SystemRoleName.MANGAKA, SystemRoleName.ASSISTANT);

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private SystemRoleRepository systemRoleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public Map<String, Object> createAccount(AccountRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        SystemRoleName requestedRole = parsePublicRole(request.getRequestedRole());
        if (accountRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new DuplicateEmailException("Email already exists");
        }

        Account account = new Account();
        account.setFirstName(request.getFirstName());
        account.setLastName(request.getLastName());
        account.setPhoneNumber(request.getPhoneNumber());
        account.setEmail(normalizedEmail);
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setStatus(AccountStatus.PENDING);
        account.setRequestedRole(requestedRole.name());
        account.setSystemRole(new ArrayList<>());
        return Map.of("account", AccountResponse.from(accountRepository.save(account)));
    }

    @Override
    public Map<String, Object> login(AccountLoginRequest request) {
        Account account = accountRepository.findByEmailIgnoreCase(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new BadCredentialsException("Email or password is invalid"));
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new BadCredentialsException("Email or password is invalid");
        }

        if (AccountStatus.PENDING.matches(account.getStatus())) {
            throw new AccountStatusException("ACCOUNT_PENDING", "Account is pending approval", null);
        }
        if (AccountStatus.REJECTED.matches(account.getStatus())) {
            throw new AccountStatusException(
                    "ACCOUNT_REJECTED", "Account registration was rejected", account.getRejectionReason());
        }
        if (AccountStatus.INACTIVE.matches(account.getStatus())) {
            throw new AccountStatusException("ACCOUNT_INACTIVE", "Account is inactive", null);
        }
        if (!account.isEnabled()) {
            throw new AccountStatusException("ACCOUNT_UNAVAILABLE", "Account is unavailable", null);
        }

        return Map.of("token", authenticationService.generateToken(account), "account", AccountResponse.from(account));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountRequests(String reviewerEmail, String status) {
        Set<SystemRoleName> allowedRoles = allowedRoles(getReviewer(reviewerEmail));
        List<String> roleNames = allowedRoles.stream().map(SystemRoleName::name).toList();
        AccountStatus requestedStatus = AccountStatus.PENDING;
        if (status != null && !status.isBlank()) {
            requestedStatus = AccountStatus.from(status);
        }
        return accountRepository.findByStatusIgnoreCaseAndRequestedRoleInOrderByIdDesc(
                        requestedStatus.name(), roleNames).stream()
                .map(AccountResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public AccountResponse approveAccountRole(Long accountId, String reviewerEmail) {
        Account reviewer = getReviewer(reviewerEmail);
        Account account = getPendingAccountForUpdate(accountId);
        SystemRoleName requestedRole = parseStoredPublicRole(account.getRequestedRole());
        enforceScope(reviewer, requestedRole);
        SystemRole role = systemRoleRepository.findAllByRoleNameIgnoreCase(requestedRole.name()).stream()
                .findFirst()
                .orElseThrow(() -> new AccountStateConflictException(
                        "System role " + requestedRole.name() + " is not configured"));

        account.setStatus(AccountStatus.ACTIVE);
        account.setSystemRole(new ArrayList<>(List.of(role)));
        account.setApprovedById(reviewer.getId());
        account.setApprovedAt(LocalDateTime.now());
        account.setRejectionReason(null);
        account.setRejectedById(null);
        account.setRejectedAt(null);
        Account saved = accountRepository.save(account);
        notificationService.createNotification(
                saved, "Your account has been approved and activated with the " + requestedRole.name() + " role.");
        return AccountResponse.from(saved);
    }

    @Override
    @Transactional
    public AccountResponse rejectAccountRole(Long accountId, String reviewerEmail, String reason) {
        Account reviewer = getReviewer(reviewerEmail);
        Account account = getPendingAccountForUpdate(accountId);
        SystemRoleName requestedRole = parseStoredPublicRole(account.getRequestedRole());
        enforceScope(reviewer, requestedRole);
        String trimmedReason = reason == null ? null : reason.trim();
        if (trimmedReason == null || trimmedReason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        if (trimmedReason.length() > 1000) {
            throw new IllegalArgumentException("Rejection reason must not exceed 1000 characters");
        }

        account.setStatus(AccountStatus.REJECTED);
        account.setSystemRole(new ArrayList<>());
        account.setRejectionReason(trimmedReason);
        account.setRejectedById(reviewer.getId());
        account.setRejectedAt(LocalDateTime.now());
        account.setApprovedById(null);
        account.setApprovedAt(null);
        Account saved = accountRepository.save(account);
        notificationService.createNotification(saved, "Your account registration was rejected: " + trimmedReason);
        return AccountResponse.from(saved);
    }

    @Override
    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public void deactivateAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setStatus(AccountStatus.INACTIVE);
        accountRepository.save(account);
    }

    @Override
    public void activateAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private SystemRoleName parsePublicRole(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidPublicRoleException("Requested role is required");
        }
        SystemRoleName role;
        try {
            role = SystemRoleName.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new InvalidPublicRoleException("Invalid public requested role: " + value);
        }
        if (!PUBLIC_ROLES.contains(role)) {
            throw new InvalidPublicRoleException("Invalid public requested role: " + value);
        }
        return role;
    }

    private SystemRoleName parseStoredPublicRole(String value) {
        try {
            return parsePublicRole(value);
        } catch (InvalidPublicRoleException exception) {
            throw new AccountStateConflictException("Account has an invalid stored requested role");
        }
    }

    private Account getReviewer(String reviewerEmail) {
        return accountRepository.findByEmailIgnoreCase(normalizeEmail(reviewerEmail))
                .orElseThrow(() -> new AccessDeniedException("Authenticated reviewer account was not found"));
    }

    private Account getPendingAccountForUpdate(Long accountId) {
        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!AccountStatus.PENDING.matches(account.getStatus())) {
            throw new AccountStateConflictException("Only PENDING accounts can be processed");
        }
        return account;
    }

    private Set<SystemRoleName> allowedRoles(Account reviewer) {
        if (reviewer.hasRole(SystemRoleName.ADMIN)) {
            return PUBLIC_ROLES;
        }
        if (reviewer.hasRole(SystemRoleName.MANAGER)) {
            return MANAGER_SCOPED_ROLES;
        }
        throw new AccessDeniedException("Insufficient permission to process account requests");
    }

    private void enforceScope(Account reviewer, SystemRoleName requestedRole) {
        if (!allowedRoles(reviewer).contains(requestedRole)) {
            throw new AccessDeniedException("Requested account role is outside the reviewer's scope");
        }
    }
}

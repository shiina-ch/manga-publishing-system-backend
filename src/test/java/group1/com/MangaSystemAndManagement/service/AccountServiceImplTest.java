package group1.com.MangaSystemAndManagement.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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
import group1.com.MangaSystemAndManagement.service.impl.AccountServiceImpl;
import group1.com.MangaSystemAndManagement.service.interfaces.NotificationService;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock AccountRepository accountRepository;
    @Mock SystemRoleRepository systemRoleRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationService authenticationService;
    @Mock NotificationService notificationService;

    private AccountServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AccountServiceImpl();
        ReflectionTestUtils.setField(service, "accountRepository", accountRepository);
        ReflectionTestUtils.setField(service, "systemRoleRepository", systemRoleRepository);
        ReflectionTestUtils.setField(service, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(service, "authenticationService", authenticationService);
        ReflectionTestUtils.setField(service, "notificationService", notificationService);
    }

    static Stream<String> publicRoles() {
        return Stream.of("MANAGER", "TANTOU_EDITOR", "EDITORIAL_BOARD_MEMBER", "MANGAKA", "ASSISTANT");
    }

    @ParameterizedTest
    @MethodSource("publicRoles")
    void registersEveryPublicRoleAsPendingWithoutActualRoleOrToken(String role) {
        when(accountRepository.findByEmailIgnoreCase("user@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Strong1!")).thenReturn("hash");
        when(accountRepository.save(any())).thenAnswer(invocation -> {
            Account saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        Map<String, Object> result = service.createAccount(request(role));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        Account saved = captor.getValue();
        assertEquals(AccountStatus.PENDING.name(), saved.getStatus());
        assertEquals(role, saved.getRequestedRole());
        assertTrue(saved.getSystemRole().isEmpty());
        assertEquals("user@gmail.com", saved.getEmail());
        assertFalse(result.containsKey("token"));
        assertInstanceOf(AccountResponse.class, result.get("account"));
        assertNotEquals("hash", result.get("account").toString());
    }

    static Stream<String> invalidPublicRoles() {
        return Stream.of("ADMIN", "TANTOR", "EDITOR", "UNKNOWN", " mangaka ", "mangaka", "");
    }

    @ParameterizedTest
    @MethodSource("invalidPublicRoles")
    void rejectsNonCanonicalPublicRoles(String role) {
        assertThrows(InvalidPublicRoleException.class, () -> service.createAccount(request(role)));
        verifyNoInteractions(accountRepository);
    }

    @Test
    void rejectsNullRequestedRole() {
        assertThrows(InvalidPublicRoleException.class, () -> service.createAccount(request(null)));
    }

    @Test
    void duplicateNormalizedEmailConflicts() {
        AccountRequest request = request("MANGAKA");
        request.setEmail(" User@Gmail.com ");
        when(accountRepository.findByEmailIgnoreCase("user@gmail.com")).thenReturn(Optional.of(new Account()));
        assertThrows(DuplicateEmailException.class, () -> service.createAccount(request));
    }

    static Stream<Arguments> managerApprovalCases() {
        return Stream.of(
                Arguments.of(SystemRoleName.MANGAKA, true),
                Arguments.of(SystemRoleName.ASSISTANT, true),
                Arguments.of(SystemRoleName.MANAGER, false),
                Arguments.of(SystemRoleName.TANTOU_EDITOR, false),
                Arguments.of(SystemRoleName.EDITORIAL_BOARD_MEMBER, false));
    }

    @ParameterizedTest
    @MethodSource("managerApprovalCases")
    void managerApprovalScopeIsEnforced(SystemRoleName requestedRole, boolean allowed) {
        Account reviewer = account(1, "manager@gmail.com", AccountStatus.ACTIVE, SystemRoleName.MANAGER);
        Account target = pending(2, requestedRole);
        when(accountRepository.findByEmailIgnoreCase("manager@gmail.com")).thenReturn(Optional.of(reviewer));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(target));

        if (!allowed) {
            assertThrows(AccessDeniedException.class,
                    () -> service.approveAccountRole(2L, "manager@gmail.com"));
            verify(accountRepository, never()).save(target);
            return;
        }

        SystemRole role = role(requestedRole);
        when(systemRoleRepository.findAllByRoleNameIgnoreCase(requestedRole.name())).thenReturn(List.of(role));
        when(accountRepository.save(target)).thenReturn(target);
        AccountResponse result = service.approveAccountRole(2L, "manager@gmail.com");
        assertEquals(AccountStatus.ACTIVE.name(), target.getStatus());
        assertEquals(List.of(role), target.getSystemRole());
        assertEquals(1L, target.getApprovedById());
        assertNotNull(target.getApprovedAt());
        assertEquals(requestedRole.name(), result.requestedRole());
    }

    @ParameterizedTest
    @MethodSource("publicRoles")
    void adminApprovesEveryPublicRoleFromStoredRequestedRole(String roleName) {
        SystemRoleName requestedRole = SystemRoleName.valueOf(roleName);
        Account admin = account(1, "admin@gmail.com", AccountStatus.ACTIVE, SystemRoleName.ADMIN);
        Account target = pending(2, requestedRole);
        SystemRole assignedRole = role(requestedRole);
        when(accountRepository.findByEmailIgnoreCase("admin@gmail.com")).thenReturn(Optional.of(admin));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(target));
        when(systemRoleRepository.findAllByRoleNameIgnoreCase(roleName)).thenReturn(List.of(assignedRole));
        when(accountRepository.save(target)).thenReturn(target);

        service.approveAccountRole(2L, "admin@gmail.com");

        assertEquals(1, target.getSystemRole().size());
        assertSame(assignedRole, target.getSystemRole().get(0));
        verify(notificationService).createNotification(eq(target), contains(roleName));
    }

    @Test
    void approvalMissingAndNonPendingHaveCorrectDomainFailures() {
        Account admin = account(1, "admin@gmail.com", AccountStatus.ACTIVE, SystemRoleName.ADMIN);
        when(accountRepository.findByEmailIgnoreCase("admin@gmail.com")).thenReturn(Optional.of(admin));
        when(accountRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.approveAccountRole(99L, "admin@gmail.com"));

        Account active = pending(2, SystemRoleName.MANGAKA);
        active.setStatus(AccountStatus.ACTIVE);
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(active));
        assertThrows(AccountStateConflictException.class,
                () -> service.approveAccountRole(2L, "admin@gmail.com"));
    }

    static Stream<Arguments> managerRejectionCases() {
        return managerApprovalCases();
    }

    @ParameterizedTest
    @MethodSource("managerRejectionCases")
    void managerRejectionScopeIsEnforced(SystemRoleName requestedRole, boolean allowed) {
        Account reviewer = account(1, "manager@gmail.com", AccountStatus.ACTIVE, SystemRoleName.MANAGER);
        Account target = pending(2, requestedRole);
        when(accountRepository.findByEmailIgnoreCase("manager@gmail.com")).thenReturn(Optional.of(reviewer));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(target));
        if (!allowed) {
            assertThrows(AccessDeniedException.class,
                    () -> service.rejectAccountRole(2L, "manager@gmail.com", "reason"));
            return;
        }
        when(accountRepository.save(target)).thenReturn(target);
        service.rejectAccountRole(2L, "manager@gmail.com", "  missing information  ");
        assertEquals(AccountStatus.REJECTED.name(), target.getStatus());
        assertTrue(target.getSystemRole().isEmpty());
        assertEquals("missing information", target.getRejectionReason());
        assertEquals(1L, target.getRejectedById());
        assertNotNull(target.getRejectedAt());
        verify(notificationService).createNotification(eq(target), contains("missing information"));
    }

    @ParameterizedTest
    @MethodSource("publicRoles")
    void adminRejectsEveryPublicRole(String roleName) {
        Account admin = account(1, "admin@gmail.com", AccountStatus.ACTIVE, SystemRoleName.ADMIN);
        Account target = pending(2, SystemRoleName.valueOf(roleName));
        when(accountRepository.findByEmailIgnoreCase("admin@gmail.com")).thenReturn(Optional.of(admin));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(target));
        when(accountRepository.save(target)).thenReturn(target);
        service.rejectAccountRole(2L, "admin@gmail.com", "reason");
        assertEquals(AccountStatus.REJECTED.name(), target.getStatus());
    }

    @Test
    void rejectionValidatesReasonAndPendingState() {
        Account admin = account(1, "admin@gmail.com", AccountStatus.ACTIVE, SystemRoleName.ADMIN);
        when(accountRepository.findByEmailIgnoreCase("admin@gmail.com")).thenReturn(Optional.of(admin));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(pending(2, SystemRoleName.MANGAKA)));
        assertThrows(IllegalArgumentException.class,
                () -> service.rejectAccountRole(2L, "admin@gmail.com", "   "));
        assertThrows(IllegalArgumentException.class,
                () -> service.rejectAccountRole(2L, "admin@gmail.com", "x".repeat(1001)));
    }

    @Test
    void managerListingIsAlwaysLimitedToMangakaAndAssistant() {
        Account manager = account(1, "manager@gmail.com", AccountStatus.ACTIVE, SystemRoleName.MANAGER);
        when(accountRepository.findByEmailIgnoreCase("manager@gmail.com")).thenReturn(Optional.of(manager));
        when(accountRepository.findByStatusIgnoreCaseAndRequestedRoleInOrderByIdDesc(eq("PENDING"), any()))
                .thenReturn(List.of(pending(2, SystemRoleName.MANGAKA)));
        service.getAccountRequests("manager@gmail.com", null);
        ArgumentCaptor<List<String>> roles = ArgumentCaptor.forClass(List.class);
        verify(accountRepository).findByStatusIgnoreCaseAndRequestedRoleInOrderByIdDesc(eq("PENDING"), roles.capture());
        assertEquals(2, roles.getValue().size());
        assertTrue(roles.getValue().containsAll(List.of("MANGAKA", "ASSISTANT")));
    }

    @Test
    void adminListingIncludesAllFivePublicRoles() {
        Account admin = account(1, "admin@gmail.com", AccountStatus.ACTIVE, SystemRoleName.ADMIN);
        when(accountRepository.findByEmailIgnoreCase("admin@gmail.com")).thenReturn(Optional.of(admin));
        when(accountRepository.findByStatusIgnoreCaseAndRequestedRoleInOrderByIdDesc(eq("PENDING"), any()))
                .thenReturn(List.of());
        service.getAccountRequests("admin@gmail.com", "PENDING");
        ArgumentCaptor<List<String>> roles = ArgumentCaptor.forClass(List.class);
        verify(accountRepository).findByStatusIgnoreCaseAndRequestedRoleInOrderByIdDesc(eq("PENDING"), roles.capture());
        assertEquals(5, roles.getValue().size());
        assertFalse(roles.getValue().contains("ADMIN"));
    }

    @Test
    void nonReviewerCannotListRequests() {
        Account editor = account(1, "editor@gmail.com", AccountStatus.ACTIVE, SystemRoleName.TANTOU_EDITOR);
        when(accountRepository.findByEmailIgnoreCase("editor@gmail.com")).thenReturn(Optional.of(editor));
        assertThrows(AccessDeniedException.class,
                () -> service.getAccountRequests("editor@gmail.com", null));
    }

    @Test
    void loginChecksPasswordBeforeRejectedState() {
        Account rejected = account(2, "user@gmail.com", AccountStatus.REJECTED, SystemRoleName.MANGAKA);
        rejected.setPassword("hash");
        rejected.setRejectionReason("Portfolio missing");
        when(accountRepository.findByEmailIgnoreCase("user@gmail.com")).thenReturn(Optional.of(rejected));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);
        assertThrows(BadCredentialsException.class,
                () -> service.login(new AccountLoginRequest("user@gmail.com", "wrong")));

        when(passwordEncoder.matches("correct", "hash")).thenReturn(true);
        AccountStatusException exception = assertThrows(AccountStatusException.class,
                () -> service.login(new AccountLoginRequest("user@gmail.com", "correct")));
        assertEquals("ACCOUNT_REJECTED", exception.getErrorCode());
        assertEquals("Portfolio missing", exception.getRejectionReason());
        verifyNoInteractions(authenticationService);
    }

    @ParameterizedTest
    @MethodSource("disabledStatuses")
    void correctPasswordReturnsSpecificDisabledStatus(AccountStatus status, String code) {
        Account account = account(2, "user@gmail.com", status, SystemRoleName.MANGAKA);
        account.setPassword("hash");
        when(accountRepository.findByEmailIgnoreCase("user@gmail.com")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("correct", "hash")).thenReturn(true);
        AccountStatusException exception = assertThrows(AccountStatusException.class,
                () -> service.login(new AccountLoginRequest("user@gmail.com", "correct")));
        assertEquals(code, exception.getErrorCode());
    }

    static Stream<Arguments> disabledStatuses() {
        return Stream.of(
                Arguments.of(AccountStatus.PENDING, "ACCOUNT_PENDING"),
                Arguments.of(AccountStatus.INACTIVE, "ACCOUNT_INACTIVE"));
    }

    @Test
    void activeLoginReturnsTokenAndSafeAccount() {
        Account active = account(2, "user@gmail.com", AccountStatus.ACTIVE, SystemRoleName.MANGAKA);
        active.setPassword("hash");
        when(accountRepository.findByEmailIgnoreCase("user@gmail.com")).thenReturn(Optional.of(active));
        when(passwordEncoder.matches("correct", "hash")).thenReturn(true);
        when(authenticationService.generateToken(active)).thenReturn("jwt");
        Map<String, Object> result = service.login(new AccountLoginRequest("user@gmail.com", "correct"));
        assertEquals("jwt", result.get("token"));
        assertInstanceOf(AccountResponse.class, result.get("account"));
    }

    private AccountRequest request(String role) {
        AccountRequest request = new AccountRequest();
        request.setFirstName("First");
        request.setLastName("Last");
        request.setPhoneNumber("0123456789");
        request.setEmail("user@gmail.com");
        request.setPassword("Strong1!");
        request.setRequestedRole(role);
        return request;
    }

    private Account pending(long id, SystemRoleName role) {
        Account account = new Account();
        account.setId(id);
        account.setRequestedRole(role.name());
        account.setStatus(AccountStatus.PENDING);
        account.setSystemRole(new ArrayList<>());
        return account;
    }

    private Account account(long id, String email, AccountStatus status, SystemRoleName roleName) {
        Account account = new Account();
        account.setId(id);
        account.setEmail(email);
        account.setStatus(status);
        account.setSystemRole(new ArrayList<>(List.of(role(roleName))));
        return account;
    }

    private SystemRole role(SystemRoleName name) {
        SystemRole role = new SystemRole();
        role.setRoleName(name.name());
        return role;
    }
}

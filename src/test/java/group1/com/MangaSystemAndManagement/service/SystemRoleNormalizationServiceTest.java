package group1.com.MangaSystemAndManagement.service;

import group1.com.MangaSystemAndManagement.model.Account;
import group1.com.MangaSystemAndManagement.model.SystemRole;
import group1.com.MangaSystemAndManagement.repository.AccountRepository;
import group1.com.MangaSystemAndManagement.repository.SystemRoleRepository;
import group1.com.MangaSystemAndManagement.service.impl.SystemRoleNormalizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemRoleNormalizationServiceTest {

    @Mock
    private SystemRoleRepository systemRoleRepository;

    @Mock
    private AccountRepository accountRepository;

    private SystemRoleNormalizationService service;

    @BeforeEach
    void setUp() {
        service = new SystemRoleNormalizationService(systemRoleRepository, accountRepository);
    }

    @Test
    void legacyOnlyRoleIsRenamedInPlace() {
        SystemRole legacy = role(7L, "TANTOR");
        Account account = account(legacy);
        when(systemRoleRepository.findAll()).thenReturn(List.of(legacy));
        when(accountRepository.findAll()).thenReturn(List.of(account));

        service.normalizeLegacyRoles();

        assertEquals(7L, legacy.getId());
        assertEquals("TANTOU_EDITOR", legacy.getRoleName());
        assertSame(legacy, account.getSystemRole().get(0));
        verify(systemRoleRepository).save(legacy);
        verify(systemRoleRepository, never()).delete(legacy);
        verify(accountRepository, never()).save(account);
    }

    @Test
    void legacyAndCanonicalRowsAreMergedIntoCanonicalRow() {
        SystemRole legacy = role(7L, "TANTOR");
        SystemRole canonical = role(8L, "TANTOU_EDITOR");
        Account account = account(legacy, canonical, legacy);
        when(systemRoleRepository.findAll()).thenReturn(List.of(legacy, canonical));
        when(accountRepository.findAll()).thenReturn(List.of(account));

        service.normalizeLegacyRoles();

        assertEquals(List.of(canonical), account.getSystemRole());
        verify(accountRepository).save(account);
        verify(accountRepository).flush();
        verify(systemRoleRepository).delete(legacy);
    }

    @Test
    void duplicateReferenceToOneCanonicalRowIsRemoved() {
        SystemRole canonical = role(8L, "TANTOU_EDITOR");
        Account account = account(canonical, canonical);
        stub(List.of(canonical), List.of(account));

        service.normalizeLegacyRoles();

        assertEquals(List.of(canonical), account.getSystemRole());
        verify(accountRepository).save(account);
        verify(systemRoleRepository, never()).delete(canonical);
    }

    @Test
    void duplicateReferenceToOneLegacyRowIsCanonicalizedAndRemoved() {
        SystemRole legacy = role(7L, "EDITOR");
        Account account = account(legacy, legacy);
        stub(List.of(legacy), List.of(account));

        service.normalizeLegacyRoles();

        assertEquals("EDITORIAL_BOARD_MEMBER", legacy.getRoleName());
        assertEquals(List.of(legacy), account.getSystemRole());
        verify(accountRepository).save(account);
    }

    @Test
    void lowestIdExactCanonicalRowSurvives() {
        SystemRole higherId = role(20L, "ADMIN");
        SystemRole lowerId = role(10L, "ADMIN");
        Account account = account(higherId, lowerId);
        stub(List.of(higherId, lowerId), List.of(account));

        service.normalizeLegacyRoles();

        assertEquals(List.of(lowerId), account.getSystemRole());
        verify(systemRoleRepository).delete(higherId);
        verify(systemRoleRepository, never()).delete(lowerId);
    }

    @Test
    void lowestIdLegacyRowSurvivesWhenNoExactCanonicalRowExists() {
        SystemRole higherId = role(20L, "TANTOR");
        SystemRole lowerId = role(10L, "tantor");
        Account account = account(higherId);
        stub(List.of(higherId, lowerId), List.of(account));

        service.normalizeLegacyRoles();

        assertEquals("TANTOU_EDITOR", lowerId.getRoleName());
        assertEquals(List.of(lowerId), account.getSystemRole());
        verify(systemRoleRepository).delete(higherId);
    }

    @Test
    void duplicateRowsWithoutAccountsAreMerged() {
        SystemRole legacy = role(7L, "EDITOR");
        SystemRole canonical = role(8L, "EDITORIAL_BOARD_MEMBER");
        stub(List.of(legacy, canonical), List.of());

        service.normalizeLegacyRoles();

        verify(accountRepository).flush();
        verify(systemRoleRepository).delete(legacy);
    }

    @Test
    void nullAccountRoleCollectionIsIgnored() {
        SystemRole legacy = role(7L, "TANTOR");
        Account account = new Account();
        account.setSystemRole(null);
        stub(List.of(legacy), List.of(account));

        service.normalizeLegacyRoles();

        assertEquals("TANTOU_EDITOR", legacy.getRoleName());
        verify(accountRepository, never()).save(account);
    }

    @Test
    void repeatedNormalizationIsIdempotent() {
        SystemRole legacy = role(7L, "EDITOR");
        when(systemRoleRepository.findAll()).thenReturn(List.of(legacy));
        when(accountRepository.findAll()).thenReturn(List.of());

        service.normalizeLegacyRoles();
        service.normalizeLegacyRoles();

        assertEquals("EDITORIAL_BOARD_MEMBER", legacy.getRoleName());
        verify(systemRoleRepository, times(1)).save(legacy);
        verify(systemRoleRepository, never()).delete(legacy);
    }

    private SystemRole role(long id, String name) {
        SystemRole role = new SystemRole();
        role.setId(id);
        role.setRoleName(name);
        return role;
    }

    private Account account(SystemRole... roles) {
        Account account = new Account();
        account.setSystemRole(new ArrayList<>(List.of(roles)));
        return account;
    }

    private void stub(List<SystemRole> roles, List<Account> accounts) {
        when(systemRoleRepository.findAll()).thenReturn(roles);
        when(accountRepository.findAll()).thenReturn(accounts);
    }
}

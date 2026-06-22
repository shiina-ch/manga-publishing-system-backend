package group1.com.MangaSystemAndManagement.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import group1.com.MangaSystemAndManagement.dto.request.AccountLoginRequest;
import group1.com.MangaSystemAndManagement.dto.request.AccountRequest;
import group1.com.MangaSystemAndManagement.exception.DuplicateEmailException;
import group1.com.MangaSystemAndManagement.model.Account;
import group1.com.MangaSystemAndManagement.model.SystemRole;
import group1.com.MangaSystemAndManagement.repository.AccountRepository;
import group1.com.MangaSystemAndManagement.repository.SystemRoleRepository;
import group1.com.MangaSystemAndManagement.security.service.AuthenticationService;
import group1.com.MangaSystemAndManagement.service.interfaces.AccountService;
import group1.com.MangaSystemAndManagement.service.interfaces.NotificationService;

@Service
public class AccountServiceImpl implements AccountService {

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
    public Map<String, Object> createAccount(AccountRequest request) {
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email đã tồn tại");
        }

        Account newAccount = new Account();
        newAccount.setFirstName(request.getFirstName());
        newAccount.setLastName(request.getLastName());
        newAccount.setPhoneNumber(request.getPhoneNumber());
        newAccount.setEmail(request.getEmail());
        newAccount.setPassword(passwordEncoder.encode(request.getPassword()));

        newAccount.setStatus("PENDING");
        newAccount.setRequestedRole(
            request.getRequestedRole() != null && !request.getRequestedRole().isEmpty() 
            ? request.getRequestedRole().toUpperCase() 
            : "MANGAKA"
        );
        newAccount.setSystemRole(new java.util.ArrayList<>());

        Account savedAccount = accountRepository.save(newAccount);
//        String token = authenticationService.generateToken(savedAccount);

        return Map.of(
//            "token", token,
            "account", savedAccount
        );
    }

    @Override
    public Map<String, Object> login(AccountLoginRequest loginRequest) {
        Account existedAccount = accountRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email or password is invalid"));
        
        if ("PENDING".equalsIgnoreCase(existedAccount.getStatus())) {
            throw new BadCredentialsException("Tài khoản chưa được duyệt bởi admin.");
        }
        
        if ("INACTIVE".equalsIgnoreCase(existedAccount.getStatus())) {
            throw new BadCredentialsException("Tài khoản đã bị vô hiệu hóa.");
        }
        
        if (!passwordEncoder.matches(loginRequest.getPassword(), existedAccount.getPassword())) {
            throw new BadCredentialsException("Email or password is invalid");
        }
        String token = authenticationService.generateToken(existedAccount);
        return Map.of(
            "token", token,
            "account", existedAccount
        );
    }

    @Override
    public void approveAccountRole(Long accountId, String roleName) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Support case-insensitive role names
        String upperRoleName = roleName != null ? roleName.toUpperCase() : null;
        SystemRole role = systemRoleRepository.findByRoleName(upperRoleName);
        
        if (role == null) {
            throw new RuntimeException("Role " + upperRoleName + " not found");
        }

        account.setStatus("ACTIVE");
        java.util.List<SystemRole> roles = new java.util.ArrayList<>();
        roles.add(role);
        account.setSystemRole(roles);
        accountRepository.save(account);

        if (notificationService != null) {
            notificationService.createNotification(account, "Your account has been approved and activated with the " + upperRoleName + " role.");
        }
    }

    @Override
    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public void deactivateAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setStatus("INACTIVE");
        accountRepository.save(account);
    }
}

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

    @Override
    public Map<String, Object> createAccount(AccountRequest request) {
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email đã tồn tại");
        }

        SystemRole role = systemRoleRepository.findByRoleName(("MANGAKA"));

        Account newAccount = new Account();
        newAccount.setFirstName(request.getFirstName());
        newAccount.setLastName(request.getLastName());
        newAccount.setPhoneNumber(request.getPhoneNumber());
        newAccount.setEmail(request.getEmail());
        newAccount.setPassword(passwordEncoder.encode(request.getPassword()));
        newAccount.setAddress(request.getAddress());
        newAccount.setSystemRole(role != null ? List.of(role) : List.of());

        Account savedAccount = accountRepository.save(newAccount);
        String token = authenticationService.generateToken(savedAccount);

        return Map.of(
            "token", token,
            "account", savedAccount
        );
    }

    @Override
    public Map<String, Object> login(AccountLoginRequest loginRequest) {
        Account existedAccount = accountRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email or password is invalid"));
        if (!passwordEncoder.matches(loginRequest.getPassword(), existedAccount.getPassword())) {
            throw new BadCredentialsException("Email or password is invalid");
        }
        String token = authenticationService.generateToken(existedAccount);
        return Map.of(
            "token", token,
            "account", existedAccount
        );
    }
}

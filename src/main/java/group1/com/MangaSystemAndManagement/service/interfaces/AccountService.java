package group1.com.MangaSystemAndManagement.service.interfaces;

import java.util.Map;

import group1.com.MangaSystemAndManagement.dto.request.AccountLoginRequest;
import group1.com.MangaSystemAndManagement.dto.request.AccountRequest;

public interface AccountService {
    public Map<String, Object> createAccount(AccountRequest request);
    public Map<String, Object> login(AccountLoginRequest loginRequest);
    void approveAccountRole(Long accountId, String roleName);
}

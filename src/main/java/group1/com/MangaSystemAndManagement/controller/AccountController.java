package group1.com.MangaSystemAndManagement.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import group1.com.MangaSystemAndManagement.dto.request.AccountLoginRequest;
import group1.com.MangaSystemAndManagement.dto.request.AccountRequest;
import group1.com.MangaSystemAndManagement.dto.response.ResponseBase;
import group1.com.MangaSystemAndManagement.service.interfaces.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Accounts", description = "Account managements APIs")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/auth/accounts")
    public ResponseEntity<ResponseBase> createAccount(@RequestBody AccountRequest request) {
        try {
            Map<String, Object> result = accountService.createAccount(request);
            ResponseBase response = new ResponseBase();
            response.setCode(201);
            response.setMessage("Chúc mừng tạo tài khoản thành công");
            response.setData(result);
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            ResponseBase response = new ResponseBase();
            response.setCode(409);
            response.setMessage(e.getMessage());
            response.setData(null);
            return ResponseEntity.status(409).body(response);
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ResponseBase> loginAccount(@RequestBody AccountLoginRequest loginRequest) {
        try {
            Map<String, Object> result = accountService.login(loginRequest);
            ResponseBase response = new ResponseBase();
            response.setCode(200);
            response.setMessage("Đăng nhập thành công");
            response.setData(result);
            return ResponseEntity.status(200).body(response);
        } catch (Exception e) {
            ResponseBase response = new ResponseBase();
            response.setCode(401);
            response.setMessage(e.getMessage());
            response.setData(null);
            return ResponseEntity.status(401).body(response);
        }

    }

    @PostMapping("/admin/accounts/{accountId}/approve")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<ResponseBase> approveAccountRole(@PathVariable Long accountId,
            @RequestParam String roleName) {
        try {
            accountService.approveAccountRole(accountId, roleName);
            ResponseBase response = new ResponseBase();
            response.setCode(200);
            response.setMessage("Tài khoản đã được duyệt và gán quyền " + roleName);
            response.setData(null);
            return ResponseEntity.status(200).body(response);
        } catch (Exception e) {
            ResponseBase response = new ResponseBase();
            response.setCode(400);
            response.setMessage(e.getMessage() != null ? e.getMessage() : e.toString());
            response.setData(null);
            return ResponseEntity.status(400).body(response);
        }
    }

    @GetMapping("/admin/accounts")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseBase> getAllAccounts() {
        try {
            java.util.List<group1.com.MangaSystemAndManagement.model.Account> accounts = accountService.getAllAccounts();
            ResponseBase response = new ResponseBase();
            response.setCode(200);
            response.setMessage("Lấy danh sách tài khoản thành công");
            response.setData(accounts);
            return ResponseEntity.status(200).body(response);
        } catch (Exception e) {
            ResponseBase response = new ResponseBase();
            response.setCode(400);
            response.setMessage(e.getMessage());
            response.setData(null);
            return ResponseEntity.status(400).body(response);
        }
    }

    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<ResponseBase> getAccountById(@PathVariable Long accountId) {
        try {
            group1.com.MangaSystemAndManagement.model.Account account = accountService.getAccountById(accountId);
            ResponseBase response = new ResponseBase();
            response.setCode(200);
            response.setMessage("Lấy thông tin tài khoản thành công");
            response.setData(account);
            return ResponseEntity.status(200).body(response);
        } catch (Exception e) {
            ResponseBase response = new ResponseBase();
            response.setCode(404);
            response.setMessage(e.getMessage());
            response.setData(null);
            return ResponseEntity.status(404).body(response);
        }
    }

    @PostMapping("/admin/accounts/{accountId}/deactivate")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    public ResponseEntity<ResponseBase> deactivateAccount(@PathVariable Long accountId) {
        try {
            accountService.deactivateAccount(accountId);
            ResponseBase response = new ResponseBase();
            response.setCode(200);
            response.setMessage("Tài khoản đã được vô hiệu hóa thành công");
            response.setData(null);
            return ResponseEntity.status(200).body(response);
        } catch (Exception e) {
            ResponseBase response = new ResponseBase();
            response.setCode(400);
            response.setMessage(e.getMessage());
            response.setData(null);
            return ResponseEntity.status(400).body(response);
        }
    }
}
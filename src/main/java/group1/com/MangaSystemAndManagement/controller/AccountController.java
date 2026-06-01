package group1.com.MangaSystemAndManagement.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ResponseBase> createAccount(@RequestBody AccountRequest request){
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
    public ResponseEntity<ResponseBase> loginAccount(@RequestBody AccountLoginRequest loginRequest){
        try {
            Map<String,Object> result = accountService.login(loginRequest);
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
}
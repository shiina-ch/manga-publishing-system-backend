package group1.com.MangaSystemAndManagement.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import group1.com.MangaSystemAndManagement.exception.AccountStateConflictException;
import group1.com.MangaSystemAndManagement.exception.AccountStatusException;
import group1.com.MangaSystemAndManagement.exception.DuplicateEmailException;
import group1.com.MangaSystemAndManagement.exception.GlobalExceptionHandler;
import group1.com.MangaSystemAndManagement.exception.InvalidPublicRoleException;
import group1.com.MangaSystemAndManagement.exception.ResourceNotFoundException;
import group1.com.MangaSystemAndManagement.service.interfaces.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AccountControllerContractTest {

    @Mock AccountService accountService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AccountController(accountService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void registrationValidationFailureIs400() throws Exception {
        mockMvc.perform(post("/api/auth/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegistrationJson(null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void invalidPublicRoleIs400AndDuplicateEmailIs409() throws Exception {
        when(accountService.createAccount(any())).thenThrow(new InvalidPublicRoleException("Invalid role"));
        mockMvc.perform(post("/api/auth/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegistrationJson("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        reset(accountService);
        when(accountService.createAccount(any())).thenThrow(new DuplicateEmailException("duplicate"));
        mockMvc.perform(post("/api/auth/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegistrationJson("MANGAKA")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_EMAIL"));
    }

    @Test
    void rejectedLoginContractDistinguishesWrongAndCorrectPassword() throws Exception {
        when(accountService.login(any())).thenThrow(new org.springframework.security.authentication.BadCredentialsException("bad"));
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@gmail.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.details").doesNotExist());

        reset(accountService);
        when(accountService.login(any())).thenThrow(
                new AccountStatusException("ACCOUNT_REJECTED", "Account registration was rejected", "Missing portfolio"));
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@gmail.com\",\"password\":\"correct\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCOUNT_REJECTED"))
                .andExpect(jsonPath("$.details.rejectionReason").value("Missing portfolio"));
    }

    @Test
    void approvalDomainFailuresMapTo403404And409() throws Exception {
        var authentication = new UsernamePasswordAuthenticationToken("manager@gmail.com", null);
        when(accountService.approveAccountRole(1L, "manager@gmail.com"))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("scope"));
        mockMvc.perform(post("/api/account-requests/1/approve").principal(authentication))
                .andExpect(status().isForbidden());

        when(accountService.approveAccountRole(2L, "manager@gmail.com"))
                .thenThrow(new ResourceNotFoundException("missing"));
        mockMvc.perform(post("/api/account-requests/2/approve").principal(authentication))
                .andExpect(status().isNotFound());

        when(accountService.approveAccountRole(3L, "manager@gmail.com"))
                .thenThrow(new AccountStateConflictException("not pending"));
        mockMvc.perform(post("/api/account-requests/3/approve").principal(authentication))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectionReasonValidationIs400() throws Exception {
        var authentication = new UsernamePasswordAuthenticationToken("admin@gmail.com", null);
        mockMvc.perform(post("/api/account-requests/1/reject")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"   \"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/account-requests/1/reject")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("reason", "x".repeat(1001)))))
                .andExpect(status().isBadRequest());
    }

    private String validRegistrationJson(String role) throws Exception {
        java.util.Map<String, Object> request = new java.util.LinkedHashMap<>();
        request.put("firstName", "First");
        request.put("lastName", "Last");
        request.put("phoneNumber", "0123456789");
        request.put("email", "user@gmail.com");
        request.put("password", "StrongPassword1!");
        request.put("requestedRole", role);
        return objectMapper.writeValueAsString(request);
    }
}

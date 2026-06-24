package group1.com.MangaSystemAndManagement.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountRequestSecurityIntegrationTest {

    @Autowired MockMvc mockMvc;

    @Test
    void unauthenticatedListingReturns401() throws Exception {
        mockMvc.perform(get("/api/account-requests"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void nonReviewerRoleReturns403() throws Exception {
        mockMvc.perform(get("/api/account-requests")
                        .with(user("editor@gmail.com").authorities(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("TANTOU_EDITOR"))))
                .andExpect(status().isForbidden());
    }
}

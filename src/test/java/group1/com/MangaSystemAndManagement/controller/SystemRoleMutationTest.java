package group1.com.MangaSystemAndManagement.controller;

import group1.com.MangaSystemAndManagement.dto.request.SystemRoleRequest;
import group1.com.MangaSystemAndManagement.repository.SystemRoleRepository;
import group1.com.MangaSystemAndManagement.service.impl.SystemRoleServiceImpl;
import group1.com.MangaSystemAndManagement.service.interfaces.SystemRoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SystemRoleMutationTest {

    @Mock
    private SystemRoleRepository repository;

    @Mock
    private SystemRoleService service;

    @Test
    void serviceRejectsAllDefinitionMutations() {
        SystemRoleServiceImpl implementation = new SystemRoleServiceImpl(repository);
        SystemRoleRequest request = new SystemRoleRequest();

        assertThrows(UnsupportedOperationException.class, () -> implementation.create(request));
        assertThrows(UnsupportedOperationException.class, () -> implementation.update(1L, request));
        assertThrows(UnsupportedOperationException.class, () -> implementation.delete(1L));
        verifyNoInteractions(repository);
    }

    @Test
    void apiReturnsMethodNotAllowedForAllDefinitionMutations() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SystemRoleController(service)).build();

        mockMvc.perform(post("/api/systemroles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value(405));
        mockMvc.perform(put("/api/systemroles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value(405));
        mockMvc.perform(delete("/api/systemroles/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value(405));

        verifyNoInteractions(service);
    }
}

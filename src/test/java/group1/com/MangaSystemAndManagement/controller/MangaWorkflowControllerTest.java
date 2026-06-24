package group1.com.MangaSystemAndManagement.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import group1.com.MangaSystemAndManagement.exception.GlobalExceptionHandler;
import group1.com.MangaSystemAndManagement.service.interfaces.MangaWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class MangaWorkflowControllerTest {

    @Mock
    private MangaWorkflowService workflowService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MangaWorkflowController(workflowService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void submitName_forbidden_when_not_mangaka() throws Exception {
        when(workflowService.submitName(any()))
                .thenThrow(new AccessDeniedException("Only Mangaka can submit a Name"));

        mockMvc.perform(post("/api/workflow/name/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectId\":1,\"submittedById\":2,\"title\":\"Name\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void reviewName_forbidden_when_not_tantou_editor() throws Exception {
        when(workflowService.reviewName(any()))
                .thenThrow(new AccessDeniedException("Only Tantou Editors can review Names"));

        mockMvc.perform(post("/api/workflow/name/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"submissionId\":10,\"reviewerId\":3,\"decision\":\"APPROVE\"}"))
                .andExpect(status().isForbidden());
    }
}

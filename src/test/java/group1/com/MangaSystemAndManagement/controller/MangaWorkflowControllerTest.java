package group1.com.MangaSystemAndManagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import group1.com.MangaSystemAndManagement.dto.request.NameSubmissionRequest;
import group1.com.MangaSystemAndManagement.dto.request.ReviewRequest;
import group1.com.MangaSystemAndManagement.service.interfaces.MangaWorkflowService;
import org.springframework.security.access.AccessDeniedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MangaWorkflowController.class)
public class MangaWorkflowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MangaWorkflowService workflowService;

    @Test
    void submitName_forbidden_when_not_mangaka() throws Exception {
        NameSubmissionRequest req = new NameSubmissionRequest();
        req.setProjectId(1L);
        req.setSubmittedById(2L);
        req.setTitle("Name");

        // service throws AccessDeniedException when submitter is not Mangaka
        when(workflowService.submitName(any())).thenThrow(new AccessDeniedException("Only Mangaka can submit a Name"));

        mockMvc.perform(post("/api/workflow/name/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void reviewName_forbidden_when_not_tantor() throws Exception {
        ReviewRequest req = new ReviewRequest();
        req.setSubmissionId(10L);
        req.setReviewerId(3L);
        req.setDecision("APPROVE");

        // service throws AccessDeniedException when reviewer is not Tantor
        when(workflowService.reviewName(any())).thenThrow(new AccessDeniedException("Only Tantor can review Names"));

        mockMvc.perform(post("/api/workflow/name/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}

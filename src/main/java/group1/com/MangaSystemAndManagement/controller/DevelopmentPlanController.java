package group1.com.MangaSystemAndManagement.controller;

import group1.com.MangaSystemAndManagement.dto.request.DevelopmentPlanRequest;
import group1.com.MangaSystemAndManagement.dto.response.ResponseBase;
import group1.com.MangaSystemAndManagement.model.DevelopmentPlan;
import group1.com.MangaSystemAndManagement.service.interfaces.DevelopmentPlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Development Plan", description = "Development Plan management APIs")
@RequiredArgsConstructor
public class DevelopmentPlanController {

    private final DevelopmentPlanService service;

    @PostMapping("/projects/{projectId}/development-plans")
    public ResponseEntity<ResponseBase> create(@PathVariable Long projectId, @RequestBody DevelopmentPlanRequest request) {
        try {
            DevelopmentPlan result = service.createDevelopmentPlan(projectId, request);
            return ResponseEntity.status(201).body(new ResponseBase(201, "Development plan created successfully", result));
        } catch (Exception e) {
            return ResponseEntity.status(409).body(new ResponseBase(409, e.getMessage(), null));
        }
    }

    @PostMapping("/development-plans/{id}/approve")
    public ResponseEntity<ResponseBase> approve(@PathVariable Long id) {
        try {
            DevelopmentPlan result = service.approveDevelopmentPlan(id);
            return ResponseEntity.status(200).body(new ResponseBase(200, "Development plan approved successfully", result));
        } catch (Exception e) {
            return ResponseEntity.status(409).body(new ResponseBase(409, e.getMessage(), null));
        }
    }

    @GetMapping("/development-plans/{id}")
    public ResponseEntity<ResponseBase> getById(@PathVariable Long id) {
        try {
            DevelopmentPlan result = service.getDevelopmentPlan(id);
            return ResponseEntity.status(200).body(new ResponseBase(200, "Success", result));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(new ResponseBase(404, e.getMessage(), null));
        }
    }
}

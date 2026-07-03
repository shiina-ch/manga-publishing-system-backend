package group1.com.MangaSystemAndManagement.controller;

import group1.com.MangaSystemAndManagement.dto.request.ProductionPlanRequest;
import group1.com.MangaSystemAndManagement.dto.response.ResponseBase;
import group1.com.MangaSystemAndManagement.model.ProductionPlan;
import group1.com.MangaSystemAndManagement.service.interfaces.ProductionPlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Production Plan", description = "Production Plan management APIs")
@RequiredArgsConstructor
public class ProductionPlanController {

    private final ProductionPlanService service;

    @PostMapping("/projects/{projectId}/production-plans")
    public ResponseEntity<ResponseBase> create(@PathVariable Long projectId, @RequestBody ProductionPlanRequest request) {
        try {
            ProductionPlan result = service.createProductionPlan(projectId, request);
            return ResponseEntity.status(201).body(new ResponseBase(201, "Production plan created successfully", result));
        } catch (Exception e) {
            return ResponseEntity.status(409).body(new ResponseBase(409, e.getMessage(), null));
        }
    }

    @PostMapping("/production-plans/{id}/approve")
    public ResponseEntity<ResponseBase> approve(@PathVariable Long id) {
        try {
            ProductionPlan result = service.approveProductionPlan(id);
            return ResponseEntity.status(200).body(new ResponseBase(200, "Production plan approved successfully", result));
        } catch (Exception e) {
            return ResponseEntity.status(409).body(new ResponseBase(409, e.getMessage(), null));
        }
    }

    @GetMapping("/production-plans/{id}")
    public ResponseEntity<ResponseBase> getById(@PathVariable Long id) {
        try {
            ProductionPlan result = service.getProductionPlan(id);
            return ResponseEntity.status(200).body(new ResponseBase(200, "Success", result));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(new ResponseBase(404, e.getMessage(), null));
        }
    }
}

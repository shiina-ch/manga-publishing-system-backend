package group1.com.MangaSystemAndManagement.controller;
import group1.com.MangaSystemAndManagement.dto.request.ProjectMemberRequest;
import group1.com.MangaSystemAndManagement.model.ProjectMember;
import group1.com.MangaSystemAndManagement.model.ProjectMemberId;
import group1.com.MangaSystemAndManagement.service.interfaces.ProjectMemberService;
import group1.com.MangaSystemAndManagement.dto.response.ResponseBase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/projectmembers")
@Tag(name = "ProjectMember", description = "ProjectMember management APIs")
@RequiredArgsConstructor
public class ProjectMemberController {
    private final ProjectMemberService service;
    @PostMapping
    public ResponseEntity<ResponseBase> create(@RequestBody ProjectMemberRequest request) {
        try {
            ProjectMember result = service.create(request);
            return ResponseEntity.status(201).body(new ResponseBase(201, "Created successfully", result));
        } catch (Exception e) {
            return ResponseEntity.status(409).body(new ResponseBase(409, e.getMessage(), null));
        }
    }
    @GetMapping
    public ResponseEntity<ResponseBase> findAll() {
        try {
            List<ProjectMember> result = service.findAll();
            return ResponseEntity.status(200).body(new ResponseBase(200, "Success", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }
    @PostMapping("/find")
    public ResponseEntity<ResponseBase> findById(@RequestBody ProjectMemberId id) {
        try {
            return service.findById(id)
                    .map(result -> ResponseEntity.status(200).body(new ResponseBase(200, "Success", result)))
                    .orElseGet(() -> ResponseEntity.status(404).body(new ResponseBase(404, "Not found", null)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }
    @PutMapping("/update")
    public ResponseEntity<ResponseBase> update(@RequestBody ProjectMemberRequest request) {
        try {
            ProjectMember result = service.update(null, request); // TODO: Add ID passing mechanism
            return ResponseEntity.status(200).body(new ResponseBase(200, "Updated successfully", result));
        } catch (Exception e) {
            return ResponseEntity.status(409).body(new ResponseBase(409, e.getMessage(), null));
        }
    }
    @DeleteMapping("/delete")
    public ResponseEntity<ResponseBase> delete(@RequestBody ProjectMemberId id) {
        try {
            service.delete(id);
            return ResponseEntity.status(200).body(new ResponseBase(200, "Deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(409).body(new ResponseBase(409, e.getMessage(), null));
        }
    }
}

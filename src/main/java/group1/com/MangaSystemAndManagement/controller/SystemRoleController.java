package group1.com.MangaSystemAndManagement.controller;
import group1.com.MangaSystemAndManagement.dto.request.SystemRoleRequest;
import group1.com.MangaSystemAndManagement.model.SystemRole;
import group1.com.MangaSystemAndManagement.service.interfaces.SystemRoleService;
import group1.com.MangaSystemAndManagement.dto.response.ResponseBase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/systemroles")
@Tag(name = "SystemRole", description = "SystemRole management APIs")
@RequiredArgsConstructor
public class SystemRoleController {
    private final SystemRoleService service;
    @PostMapping
    public ResponseEntity<ResponseBase> create(@RequestBody SystemRoleRequest request) {
        return mutationNotAllowed();
    }
    @GetMapping
    public ResponseEntity<ResponseBase> findAll() {
        try {
            List<SystemRole> result = service.findAll();
            return ResponseEntity.status(200).body(new ResponseBase(200, "Success", result));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<ResponseBase> findById(@PathVariable Long id) {
        try {
            return service.findById(id)
                    .map(result -> ResponseEntity.status(200).body(new ResponseBase(200, "Success", result)))
                    .orElseGet(() -> ResponseEntity.status(404).body(new ResponseBase(404, "Not found", null)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<ResponseBase> update(@PathVariable Long id, @RequestBody SystemRoleRequest request) {
        return mutationNotAllowed();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseBase> delete(@PathVariable Long id) {
        return mutationNotAllowed();
    }

    private ResponseEntity<ResponseBase> mutationNotAllowed() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(new ResponseBase(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "System role definitions are read-only",
                null));
    }
}

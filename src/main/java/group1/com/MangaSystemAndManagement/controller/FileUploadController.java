package group1.com.MangaSystemAndManagement.controller;

import group1.com.MangaSystemAndManagement.dto.response.ResponseBase;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@Tag(name = "Files", description = "File upload and storage APIs")
public class FileUploadController {

    private final Path uploadPath = Paths.get("uploads");

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority(T(group1.com.MangaSystemAndManagement.model.SystemRoleName).MANGAKA.name()) " +
            "or hasAuthority(T(group1.com.MangaSystemAndManagement.model.SystemRoleName).ASSISTANT.name()) " +
            "or hasAuthority(T(group1.com.MangaSystemAndManagement.model.SystemRoleName).ADMIN.name())")
    @Operation(summary = "Upload an image file (PNG, JPG, JPEG, GIF, WEBP)", description = "Saves the file to local storage and returns its accessible URL.")
    public ResponseEntity<ResponseBase> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Validate that file is present and not empty
            if (file.isEmpty()) {
                return ResponseEntity.status(400).body(new ResponseBase(400, "File is empty", null));
            }

            // Extract file extension and validate image format
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }

            List<String> allowedExtensions = List.of(".png", ".jpg", ".jpeg", ".gif", ".webp");
            if (!allowedExtensions.contains(extension)) {
                return ResponseEntity.status(400).body(new ResponseBase(
                        400,
                        "Invalid file format. Only image formats (PNG, JPG, JPEG, GIF, WEBP) are allowed.",
                        null
                ));
            }

            // Ensure uploads folder exists
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename using UUID
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path targetLocation = this.uploadPath.resolve(uniqueFilename);

            // Copy file to the target location
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Construct dynamic public file URL
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(uniqueFilename)
                    .toUriString();

            return ResponseEntity.ok(new ResponseBase(200, "File uploaded successfully", fileUrl));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, "Failed to store file: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseBase(500, e.getMessage(), null));
        }
    }
}

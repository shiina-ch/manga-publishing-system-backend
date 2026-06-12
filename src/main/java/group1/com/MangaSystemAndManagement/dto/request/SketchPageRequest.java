package group1.com.MangaSystemAndManagement.dto.request;

import group1.com.MangaSystemAndManagement.model.*;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SketchPageRequest {
    @NotNull
    private Chapter chapter;
    private Integer pageNumber;
    @Size(max = 1000)
    private String initialSketchUrl;
    @NotNull
    private Account createdBy;
    @Size(max = 50)
    private String status;
}

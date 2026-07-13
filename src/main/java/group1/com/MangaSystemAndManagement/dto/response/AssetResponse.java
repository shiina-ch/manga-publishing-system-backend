package group1.com.MangaSystemAndManagement.dto.response;

import group1.com.MangaSystemAndManagement.model.AssetCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetResponse {
    private Long id;
    private Long projectId;
    private String name;
    private AssetCategory category;
    private String fileUrl;
}

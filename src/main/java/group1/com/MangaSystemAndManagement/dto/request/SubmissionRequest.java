package group1.com.MangaSystemAndManagement.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Request DTO để Mangaka tạo Submission (nộp file PSD).
 *
 * Luồng: User (accountId via path param) → Submission (title, planningId?) → SubmissionFiles
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionRequest {

    /** ID của Planning mà submission này thuộc về (optional) */
    private Long planningId;

    /** Tiêu đề submission */
    @Size(max = 255)
    private String title;

    /** Ghi chú / mô tả thêm */
    @Size(max = 1000)
    private String note;

    private String story;
    private String characterDescription;
    private String worldSetting;

    /** Danh sách file PSD / tài nguyên đính kèm (multipart) */
    private List<MultipartFile> files;
}

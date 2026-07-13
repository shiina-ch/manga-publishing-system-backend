package group1.com.MangaSystemAndManagement.dto.response;

import group1.com.MangaSystemAndManagement.model.FileType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmissionFileResponse {
    private Long id;
    private String originalName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private FileType fileType;
    private Integer fileOrder;

    public static SubmissionFileResponse from(
            group1.com.MangaSystemAndManagement.model.SubmissionFile f) {
        SubmissionFileResponse r = new SubmissionFileResponse();
        r.id = f.getId();
        r.originalName = f.getOriginalName();
        r.filePath = f.getFilePath();
        r.fileSize = f.getFileSize();
        r.contentType = f.getContentType();
        r.fileType = f.getFileType();
        r.fileOrder = f.getFileOrder();
        return r;
    }
}

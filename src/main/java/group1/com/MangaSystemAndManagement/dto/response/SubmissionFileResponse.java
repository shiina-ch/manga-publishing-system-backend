package group1.com.MangaSystemAndManagement.dto.response;

import group1.com.MangaSystemAndManagement.model.FileType;
import group1.com.MangaSystemAndManagement.model.SubmissionFile;
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

    public static SubmissionFileResponse from(SubmissionFile sf) {
        SubmissionFileResponse r = new SubmissionFileResponse();
        r.id = sf.getId();
        r.originalName = sf.getOriginalName();
        r.filePath = sf.getFilePath();
        r.fileSize = sf.getFileSize();
        r.contentType = sf.getContentType();
        r.fileType = sf.getFileType();
        r.fileOrder = sf.getFileOrder();
        return r;
    }
}
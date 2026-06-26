package group1.com.MangaSystemAndManagement.service.interfaces;

import group1.com.MangaSystemAndManagement.dto.request.SubmissionRequest;
import group1.com.MangaSystemAndManagement.model.Submission;

import java.util.List;
import java.util.Optional;

public interface SubmissionService {

    /**
     * Mangaka (accountId) tạo submission và upload các file PSD đính kèm.
     * Luồng: Account → Submission → SubmissionFiles
     *
     * @param accountId ID của user đang submit
     * @param request   thông tin submission + danh sách file
     * @return Submission đã được lưu kèm files
     */
    Submission submitFiles(Long accountId, SubmissionRequest request);

    Optional<Submission> findById(Long id);

    List<Submission> findAll();

    /**
     * Cập nhật metadata của submission (không thay thế files).
     */
    Submission update(Long id, SubmissionRequest request);

    void delete(Long id);

    /**
     * Admin duyệt submission và tự động tạo Project từ đó.
     */
    Submission approveAndCreateProject(Long submissionId);
}

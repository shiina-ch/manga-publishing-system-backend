package group1.com.MangaSystemAndManagement.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lưu trữ thông tin file PSD / tài nguyên đính kèm của một Submission.
 * Quan hệ: Submission (1) ←→ (N) SubmissionFile
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên file gốc do Mangaka upload (e.g. chapter01.psd) */
    private String originalName;

    /** URL công khai để truy cập file (e.g. /uploads/uuid.psd) */
    @Column(length = 1000)
    private String filePath;

    /** Kích thước file tính theo byte */
    private Long fileSize;

    /** MIME type (e.g. image/vnd.adobe.photoshop, application/octet-stream) */
    private String contentType;

    /** Submission mà file này thuộc về */
    @JsonBackReference("submission-files")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;
}

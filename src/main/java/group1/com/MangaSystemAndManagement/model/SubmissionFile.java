package group1.com.MangaSystemAndManagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single PSD / asset file attached to a {@link Submission}.
 * Carries an explicit {@link FileType} discriminator and ordering to allow
 * round-by-round comparison without walking the entire submission chain.
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

    /** Original filename supplied by the uploader (e.g. chapter01.psd). */
    private String originalName;

    /** Public URL where the file is served (e.g. /uploads/&lt;uuid&gt;.psd). */
    @Column(length = 1000)
    private String filePath;

    /** File size in bytes. */
    private Long fileSize;

    /** MIME type (e.g. image/vnd.adobe.photoshop, application/octet-stream). */
    private String contentType;

    /** Which round this file belongs to – see {@link FileType}. */
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", length = 50)
    private FileType fileType;

    /** Display order within the parent submission (0 = first / cover). */
    @Column(name = "file_order")
    private Integer fileOrder;

    /** Submission that owns this file. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    @JsonIgnore
    private Submission submission;
}

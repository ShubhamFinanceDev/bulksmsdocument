package com.bulkSms.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_details")
@Data
public class DocumentDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private long id;
    @Column(name = "uploaded_time")
    private LocalDateTime uploadedTime;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "job_id")
    private long jobId;
    @Column(name = "download_count")
    private long downloadCount;
    @Column(name = "download_url")
    private String downloadUrl;
    @Column(name = "last_download")
    private Timestamp lastDownload;
}

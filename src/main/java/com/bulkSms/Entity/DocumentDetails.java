package com.bulkSms.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "document_details")
public class DocumentDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "uploaded_time")
    private Timestamp uploadedTime;
    @Column(name = "job_id")
    private Long jobId;
    @Column(name = "download_count")
    private Long downloadCount;
    @Column(name = "category")
    private String category;
    @Column(name = "last_download")
    private Timestamp lastDownload;
    @Column(name = "sequence_number")
    private long sequenceNo;
}

package com.bulkSms.Repository;

import com.bulkSms.Entity.JobAuditTrail;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
@Transactional
public interface JobAuditTrailRepo extends JpaRepository<JobAuditTrail,Long> {

    @Modifying
    @Query("update JobAuditTrail j set j.status = :status, j.endDate = :endDate, j.message = :message where j.jobId = :jobId")
    void updateEndStatus(String message, String status, Timestamp endDate, Long jobId);

    @Modifying
    @Query("update JobAuditTrail j set j.status = :status, j.endDate = :endDate, j.message = :message where j.jobId = :jobId")
    void updateIfException(String message, String status, Timestamp endDate, Long jobId);
}

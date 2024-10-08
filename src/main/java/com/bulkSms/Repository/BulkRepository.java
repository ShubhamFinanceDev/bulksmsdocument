package com.bulkSms.Repository;

import com.bulkSms.Entity.BulkSms;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BulkRepository extends JpaRepository<BulkSms,Long> {

    @Transactional
    @Modifying
    @Query("UPDATE BulkSms b SET b.smsTimeStamp = CURRENT_TIMESTAMP, b.dataUpload.smsFlag = 'Y' WHERE b.dataUpload.id = :dataUploadId")
    void updateBulkSmsTimestampByDataUploadId(Long dataUploadId);

}

package com.bulkSms.Repository;

import com.bulkSms.Entity.DocumentReader;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional
public interface DocumentReaderRepo extends JpaRepository<DocumentReader,Long> {

    @Modifying
    @Query("UPDATE DocumentReader d SET d.downloadCount = d.downloadCount + 1, d.lastDownload = :currentDownloadTime WHERE d.fileName = :fileName")
    void updateDownloadCount(String fileName, Timestamp currentDownloadTime);

    @Query("select e from DocumentReader e where e.fileName =:loanNo")
    DocumentReader findByLoanNo(String loanNo);

    @Query("SELECT COUNT(e) FROM DocumentReader e WHERE e.downloadCount > 0")
    Long getDownloadCount();
}

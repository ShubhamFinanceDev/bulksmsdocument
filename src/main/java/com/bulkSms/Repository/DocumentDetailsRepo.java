package com.bulkSms.Repository;

import com.bulkSms.Entity.DataUpload;
import com.bulkSms.Entity.DocumentDetails;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface DocumentDetailsRepo extends JpaRepository<DocumentDetails, Long> {

    @Modifying
    @Query("UPDATE DocumentDetails d SET d.downloadCount = d.downloadCount + 1, d.lastDownload = :currentDownloadTime WHERE d.fileName = :fileName")
    void updateDownloadCount(String fileName, Timestamp currentDownloadTime);

    @Query("select e from DocumentDetails e where e.fileName =:loanNo and e.category=:category")
    DocumentDetails findByLoanNoAndCategory(String loanNo,String category);

    @Query("SELECT COUNT(d.id), d.category FROM DocumentDetails d WHERE d.downloadCount > 0 GROUP BY d.category")
    List<Object[]> countDownloadByCategory();

    @Query("select dd from DocumentDetails dd where dd.fileName=:loanNumber and dd.downloadCount > 0 and dd.category=:category")
    Optional<DocumentDetails> findDataByLoanNo(String loanNumber,String category);

    @Modifying
    @Query("UPDATE DocumentDetails d SET d.downloadCount = d.downloadCount + 1, d.lastDownload = :currentDownloadTime WHERE d.fileName = :fileName")
    void updateDownloadCountBySmsLink(String fileName, Timestamp currentDownloadTime);
    @Query("SELECT e FROM DocumentDetails e WHERE e.jobId=:jobId")
    List<DocumentDetails> finByJobId(Long jobId);
}

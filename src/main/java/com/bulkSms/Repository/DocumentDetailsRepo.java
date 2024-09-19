package com.bulkSms.Repository;


import com.bulkSms.Entity.DocumentDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.util.Optional;

public interface DocumentDetailsRepo extends JpaRepository<DocumentDetails, Long> {

    @Query("select dd from DocumentDetails dd where dd.downloadUrl=:loanDetails")
    Optional<Object> findDocumentByLoanNumber(String loanDetails);

    @Modifying
    @Query("UPDATE DocumentDetails d SET d.downloadCount = d.downloadCount + 1, d.lastDownload = :currentDownloadTime WHERE d.fileName = :fileName")
    void updateDownloadCount(String fileName, Timestamp currentDownloadTime);

    @Query("select e from DocumentDetails e where e.fileName =:loanNo")
    DocumentDetails findByLoanNo(String loanNo);

    @Query("SELECT COUNT(e) FROM DocumentDetails e WHERE e.downloadCount > 0")
    Long getDownloadCount();
    @Query("select dd from DocumentDetails dd where dd.fileName=:loanNumber and dd.downloadCount > 0")
    Optional<DocumentDetails> findDataByLoanNo(String loanNumber);

}

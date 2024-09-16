package com.bulkSms.Repository;

import com.bulkSms.Entity.DocumentReader;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface DocumentReaderRepo extends JpaRepository<DocumentReader,Long> {

    @Modifying
    @Query("UPDATE DocumentReader d SET d.downloadCount = d.downloadCount + 1 WHERE d.fileName = :fileName")
    void updateDownloadCount(String fileName);
}

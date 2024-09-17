package com.bulkSms.Repository;


import com.bulkSms.Entity.DocumentDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DocumentDetailsRepo extends JpaRepository<DocumentDetails, Long> {

    @Query("select dd from DocumentDetails dd where dd.downloadUrl=:loanDetails")
    Optional<Object> findDocumentByLoanNumber(String loanDetails);
}

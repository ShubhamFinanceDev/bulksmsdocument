package com.bulkSms.Repository;

import com.bulkSms.Entity.DataUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DataUploadRepo extends JpaRepository<DataUpload, Long> {

    @Query("select d from DataUpload d where d.certificateCategory = :userCategory and d.smsFlag = 'N'")
    List<DataUpload> findByCategoryAndSmsFlagNotSent(String userCategory);

    @Query("select d from DataUpload d where d.certificateCategory = :smsCategory and d.smsFlag = 'Y'")
    List<DataUpload> findBySmsCategory(String smsCategory);

    @Query("SELECT COUNT(c) FROM DataUpload c WHERE c.smsFlag = 'Y'")
    Long getSmsCount();

    @Query("select e from DataUpload e where e.loanNumber =:fileName")
    Optional<DataUpload> findByLoanNo(String fileName);
}
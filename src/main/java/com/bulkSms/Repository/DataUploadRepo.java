package com.bulkSms.Repository;

import com.bulkSms.Entity.DataUpload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DataUploadRepo extends JpaRepository<DataUpload, Long> {

    @Query("select d from DataUpload d where d.certificateCategory = :smsCategory and d.smsFlag = 'N' and d.loanNumber in (select e.fileName from DocumentDetails e where e.category=:smsCategory)")
    Page<DataUpload> findByCategoryAndSmsFlagNotSent(String smsCategory, Pageable pageable);

    @Query("select d from DataUpload d where d.certificateCategory = :smsCategory and d.smsFlag = 'Y'")
    List<DataUpload> findBySmsCategoryOfSendSms(String smsCategory, Pageable pageable);

    @Query("SELECT COUNT(d.id), d.certificateCategory FROM DataUpload d WHERE d.smsFlag = 'Y' GROUP BY d.certificateCategory")
    List<Object[]> countSmsByCategory();

    @Query("select e from DataUpload e where e.loanNumber =:fileName")
    Optional<DataUpload> findByLoanNo(String fileName);

    @Query("select d from DataUpload d where d.smsFlag = 'Y'")
    List<DataUpload> findByType(Pageable pageable);

    @Query("select d from DataUpload d where d.smsFlag = 'Y'")
    List<DataUpload> findByTypeOfSendSms(Pageable pageable);

    @Query("select d from DataUpload d where d.smsFlag = 'N' and d.loanNumber in (select e.fileName from DocumentDetails e)")
    List<DataUpload> findByTypeForUnsendSms(Pageable pageable);

    @Query("select d from DataUpload d where d.certificateCategory = :smsCategory and d.smsFlag = 'N'  and d.loanNumber in (select e.fileName from DocumentDetails e where e.category=:smsCategory)")
    List<DataUpload> findBySmsCategoryForUnsendSms(String smsCategory, Pageable pageable);

    @Query("select count(d) from DataUpload d where d.smsFlag = 'Y'")
    long findCount();

    @Query("select count(d) from DataUpload d where d.certificateCategory = :smsCategory and d.smsFlag = 'Y'")
    long findCountWithSmsCategory(String smsCategory);

    @Query("select count(d) from DataUpload d where d.smsFlag = 'N'  and d.loanNumber in (select e.fileName from DocumentDetails e)")
    long findUnsendSmsCountByType();

    @Query("select count(d) from DataUpload d where d.certificateCategory = :smsCategory and d.smsFlag = 'N'  and d.loanNumber in (select e.fileName from DocumentDetails e where e.category=:smsCategory)")
    long findUnsendSmsCountByCategory(String smsCategory);

    @Query("SELECT d FROM DataUpload d WHERE d.loanNumber = :loanNumber and d.certificateCategory = :smsCategory")
    Optional<DataUpload> findByloanNumber(String loanNumber, String smsCategory);
}
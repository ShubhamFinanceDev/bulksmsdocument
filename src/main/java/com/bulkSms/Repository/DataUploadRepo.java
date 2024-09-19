package com.bulkSms.Repository;

import com.bulkSms.Entity.DataUpload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DataUploadRepo extends JpaRepository<DataUpload, Long> {

    @Query("select d from DataUpload d where d.certificateCategory = :userCategory and d.smsFlag = 'N'")
    List<DataUpload> findByCategoryAndSmsFlagNotSent(String userCategory, Pageable pageable);

    @Query("select d from DataUpload d where d.certificateCategory = :smsCategory and d.smsFlag = 'Y'")
    List<DataUpload> findBySmsCategory(String smsCategory, Pageable pageable);

    @Query("select d from DataUpload d where d.smsFlag = 'Y'")
    List<DataUpload> findByType(Pageable pageable);

    @Query("select d from DataUpload d where d.smsFlag = 'N'")
    List<DataUpload> findByTypeForUnsendSms(Pageable pageable);

    @Query("select d from DataUpload d where d.certificateCategory = :smsCategory and d.smsFlag = 'N'")
    List<DataUpload> findBySmsCategoryForUnsendSms(String smsCategory, Pageable pageable);

    @Query("select count(d) from DataUpload d where d.smsFlag = 'Y'")
    long findCount();

    @Query("select count(d) from DataUpload d where d.certificateCategory = :smsCategory and d.smsFlag = 'Y'")
    long findCountWithSmsCategory(String smsCategory);

    @Query("select count(d) from DataUpload d where d.smsFlag = 'N'")
    long findUnsendSmsCountByType();

    @Query("select count(d) from DataUpload d where d.certificateCategory = :smsCategory and d.smsFlag = 'N'")
    long findUnsendSmsCountByCategory(String smsCategory);

    @Query("select count(d) from DataUpload d where d.certificateCategory = :smsCategory and d.smsFlag = 'Y'")
    long smsToBeSendCount(String smsCategory);
}
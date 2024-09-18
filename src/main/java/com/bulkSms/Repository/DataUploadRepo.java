package com.bulkSms.Repository;

import com.bulkSms.Entity.DataUpload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DataUploadRepo extends JpaRepository<DataUpload, Long> {

    @Query("select d from DataUpload d where d.certificateCategory = :userCategory and d.smsFlag = 'N'")
    Page<DataUpload> findByCategoryAndSmsFlagNotSent(String userCategory, Pageable pageable);

    @Query("select d from DataUpload d where d.certificateCategory = :smsCategory and d.smsFlag = 'Y'")
    Page<DataUpload> findBySmsCategory(String smsCategory, Pageable pageable);

    @Query("select d from DataUpload d where d.smsFlag = 'Y'")
    Page<DataUpload> findByType(Pageable pageable);
}
package com.bulkSms.Repository;

import com.bulkSms.Entity.DataUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DataUploadRepo extends JpaRepository<DataUpload, Long> {

    @Query("select d from DataUpload d where d.certificateCategory = :userCategory and d.smsFlag = 'N'")
    List<DataUpload> findByCategoryAndSmsFlagNotSent(String userCategory);
}

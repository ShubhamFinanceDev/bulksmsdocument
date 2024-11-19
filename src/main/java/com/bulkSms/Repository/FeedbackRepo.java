package com.bulkSms.Repository;

import com.bulkSms.Entity.FeedbackRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FeedbackRepo extends JpaRepository<FeedbackRecord,Long>
{

    @Query("select f from FeedbackRecord f where f.formId=:formId and f.contactNo=:contactNo")
    FeedbackRecord findByFormIdAndContactNo(String formId, String contactNo);

}

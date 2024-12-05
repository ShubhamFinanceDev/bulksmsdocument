package com.bulkSms.Repository;

import com.bulkSms.Entity.FeedbackRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FeedbackRepo extends JpaRepository<FeedbackRecord,Long>
{

    @Query(value = "SELECT * FROM feedback_record f " +
            "WHERE f.form_id = :formId " +
            "ORDER BY f.date DESC   LIMIT 1",
            nativeQuery = true)
    FeedbackRecord findByFormId(String formId);
}

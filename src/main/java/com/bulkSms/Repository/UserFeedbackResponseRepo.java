package com.bulkSms.Repository;

import com.bulkSms.Entity.UserFeedbackResponse;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserFeedbackResponseRepo extends JpaRepository<UserFeedbackResponse,Long> {
}

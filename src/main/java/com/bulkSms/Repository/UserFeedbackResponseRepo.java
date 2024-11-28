package com.bulkSms.Repository;

import com.bulkSms.Entity.UserFeedbackResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;


public interface UserFeedbackResponseRepo extends JpaRepository<UserFeedbackResponse,Long> {
    List<UserFeedbackResponse> findByLatestDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}

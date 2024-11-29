package com.bulkSms.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "feedback_record")
public class FeedbackRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long formId;
    private String contactNo;
    private String loanNo;
    private String customerName;
    private String feedbackSendFlag;
    private String feedbackSubmitFlag;
    private LocalDateTime date;
    @PrePersist
    @PreUpdate
    public void setLastUpdated() {
        this.date = LocalDateTime.now();
    }
}
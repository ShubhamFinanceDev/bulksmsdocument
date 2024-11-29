package com.bulkSms.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "feedback_response")
public class UserFeedbackResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String formId;
    private String contactNo;
    private String customerName;
    private String loanAccountNo;

    private String howDidYouHear; // How did you hear about Shubham?
    private String loanProcessClear; // Was the entire loan process explained to you clearly?
    private String employeesTrustworthy; // Did you find Shubham Employees trustworthy & informative?
    private String facedDifficulties; // Did you face any difficulties during the loan process?
    private String recommendShubham; // Would you recommend Shubham to family and friends?
    private String satisfactionLevel; // How satisfied are you with your experience with Shubham?
    private String comment;
    private String feedbackFlag;
    private LocalDateTime latestDate;
    @PrePersist
    @PreUpdate
    public void setLastUpdated() {
        this.latestDate = LocalDateTime.now();
    }
}
package com.bulkSms.Entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "feedback_response")
public class UserFeedbackResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String formId;
    private String contactNo;
    private String serviceRating;
    private String supportRating;
    private String comments;
    private String feedbackFlag;

}

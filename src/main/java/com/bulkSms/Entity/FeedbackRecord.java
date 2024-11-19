package com.bulkSms.Entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "feedback_record")
public class FeedbackRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String formId;
    private String contactNo;
    private String feedbackSendFlag;
    private String feedbackSubmitFlag;
}



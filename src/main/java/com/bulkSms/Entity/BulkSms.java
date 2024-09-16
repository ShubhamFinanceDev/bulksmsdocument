package com.bulkSms.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "bulksms")
@Data
public class BulkSms {
    @jakarta.persistence.Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id")
    private Long  Id;
    @Column(name="loan_number")
    private String loanNumber;
    @Column(name="mobile_number")
    private String mobileNumber;
    @Column(name = "Certificate_Category")
    private String certificateCategory;
}

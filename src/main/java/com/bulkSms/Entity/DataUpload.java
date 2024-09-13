package com.bulkSms.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "data_upload")
@Data
public class DataUpload {
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
    @Column(name = "sms-flag")
    private String smsFlag;

    @OneToOne(mappedBy = "dataUpload", cascade = CascadeType.ALL)
    private BulkSms bulkSms;
}

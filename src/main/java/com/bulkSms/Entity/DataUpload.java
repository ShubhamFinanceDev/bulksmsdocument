package com.bulkSms.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "data_upload")
@Data
public class DataUpload {
    @jakarta.persistence.Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id")
    private Long  id;
    @Column(name="loan_number")
    private String loanNumber;
    @Column(name="mobile_number")
    private String mobileNumber;
    @Column(name = "Certificate_Category")
    private String certificateCategory;
    @Column(name = "sms_flag")
    private String smsFlag;
    @Column(name = "upload_date")
    private Date upload_date;

    @OneToOne(mappedBy = "dataUpload", cascade = CascadeType.ALL)
    private BulkSms bulkSms;
}

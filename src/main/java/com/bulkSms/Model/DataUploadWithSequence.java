package com.bulkSms.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DataUploadWithSequence {
    private  Long id;
    private String loanNumber;
    private String mobileNumber;
    private String certificateCategory;
    private Long fileSequenceNo;
}

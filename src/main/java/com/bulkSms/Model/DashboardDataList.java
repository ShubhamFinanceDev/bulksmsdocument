package com.bulkSms.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
public class DashboardDataList {
    private String loanNumber;
    private String mobileNumber;
    private String certificateCategory;
    private Long downloadCount;
    private Timestamp lastDownload;
    private LocalDateTime smsTimeStamp;

    public DashboardDataList(String loanNumber, String mobileNumber, String certificateCategory, Long downloadCount, Timestamp lastDownload, LocalDateTime smsTimeStamp) {
        this.loanNumber = loanNumber;
        this.mobileNumber = mobileNumber;
        this.certificateCategory = certificateCategory;
        this.downloadCount = downloadCount;
        this.lastDownload = lastDownload;
        this.smsTimeStamp = smsTimeStamp;
    }
}

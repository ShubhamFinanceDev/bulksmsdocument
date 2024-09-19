package com.bulkSms.Model;

import lombok.Data;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DashboardDataList {
    private String loanNo;
    private String phoneNo;
    private String category;
    private Timestamp lastDownload;
    private LocalDateTime smsTimeStamp;
    private Long downloadCount;
}

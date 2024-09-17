package com.bulkSms.Model;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class DashboardDataList {
    private String loanNo;
    private String phoneNo;
    private String category;
    private Timestamp lastDownload;
    private Long downloadCount;
}

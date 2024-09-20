package com.bulkSms.Model;

import lombok.Data;

import java.util.List;

@Data
public class SmsResponse {

    private long totalCount;

    private String msg;

    private Boolean nextPage;

    private List<Object> smsInformation;

    public SmsResponse(long totalCount, boolean nextPage, String msg, List<Object> smsInformation){
        this.totalCount = totalCount;
        this.nextPage = nextPage;
        this.msg = msg;
        this.smsInformation = smsInformation;
    }
    public SmsResponse(long totalCount, String msg, List<Object> smsInformation){
        this.totalCount = totalCount;
        this.msg = msg;
        this.smsInformation = smsInformation;
        this.nextPage = null;
    }
}

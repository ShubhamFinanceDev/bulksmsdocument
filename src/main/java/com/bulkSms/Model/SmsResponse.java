package com.bulkSms.Model;

import lombok.Data;

import java.util.List;

@Data
public class SmsResponse {

    private int totalCount;

    private String msg;

    private List<Object> smsInformation;

    public SmsResponse(int totalCount, String msg, List<Object> smsInformation){
        this.totalCount = totalCount;
        this.msg = msg;
        this.smsInformation = smsInformation;
    }

    public SmsResponse(String msg){
        this.msg = msg;
    }

}

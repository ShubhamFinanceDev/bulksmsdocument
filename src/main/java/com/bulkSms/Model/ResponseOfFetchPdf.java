package com.bulkSms.Model;

import lombok.Data;

import java.util.List;

@Data
public class ResponseOfFetchPdf {
    private CommonResponse commonResponse;
    private List<ListResponse> listOfPdfNames;
}
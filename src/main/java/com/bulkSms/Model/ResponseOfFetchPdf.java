package com.bulkSms.Model;

import lombok.Data;

import java.util.List;

@Data
public class ResponseOfFetchPdf {
    private CommonResponse commonResponse;
    private Long downloadCount;
    private Long totalCount;
    private boolean nextPage;
    private List<ListResponse> listOfPdfNames;
}

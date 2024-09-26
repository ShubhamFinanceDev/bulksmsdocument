package com.bulkSms.Model;

import lombok.Data;

import java.util.List;

@Data
public class DashboardResponse extends CommonResponse{
    private Long smsCount;
    private Long downloadCount;
    private Long totalCount;
    private boolean nextPage;

    private List<DashboardDataList> dataLists;
}

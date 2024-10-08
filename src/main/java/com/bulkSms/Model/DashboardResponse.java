package com.bulkSms.Model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardResponse extends CommonResponse{

    private Map<String, Long> smsCountByCategory;
    private Map<String, Long> downloadCountByCategory;
    private Long totalCount;
    private boolean nextPage;

    private List<DashboardDataList> dataLists;
}

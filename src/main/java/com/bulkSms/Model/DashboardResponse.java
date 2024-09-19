package com.bulkSms.Model;

import lombok.Data;

import java.util.List;

@Data
public class DashboardResponse {
    private Long smsCount;
    private Long downloadCount;

    private List<DashboardDataList> dataLists;
}

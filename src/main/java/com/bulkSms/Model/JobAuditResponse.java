package com.bulkSms.Model;

import com.bulkSms.Entity.JobAuditTrail;
import lombok.Data;

import java.util.List;

@Data
public class JobAuditResponse {
    private String msg;
    private List<JobAuditTrail> jobAuditTrailList;
}

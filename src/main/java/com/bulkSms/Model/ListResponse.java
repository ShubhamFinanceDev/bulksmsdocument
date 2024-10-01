package com.bulkSms.Model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ListResponse {
    private String fileName;
    private Long downloadCount;
    private LocalDateTime uploadTime;
    private String category;
}

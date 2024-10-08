package com.bulkSms.Model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ListResponse {
    private String fileName;
    private LocalDateTime uploadTime;
    private String category;
}

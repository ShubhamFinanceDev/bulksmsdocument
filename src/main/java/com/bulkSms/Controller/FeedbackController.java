package com.bulkSms.Controller;

import com.bulkSms.Entity.FeedbackRecord;
import com.bulkSms.Entity.UserFeedbackResponse;
import com.bulkSms.Service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/feedbackManagement")
public class FeedbackController {

    @Autowired
    private Service service;


    @GetMapping("/generate-feedback-excel")
    @ResponseBody
    public ResponseEntity<byte[]> generateFeedbackExcel(
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr) throws IOException {

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (startDateStr != null && endDateStr != null) {
            startDate = LocalDateTime.parse(startDateStr, formatter);
            endDate = LocalDateTime.parse(endDateStr, formatter);
        }

        InputStream excelFile = service.generateExcelFile(startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=feedback_response_Records.xlsx");

        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(excelFile.readAllBytes());
    }

}

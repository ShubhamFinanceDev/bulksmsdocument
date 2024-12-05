package com.bulkSms.Controller;

import com.bulkSms.Service.Service;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@RestController
@RequestMapping("/feedbackManagement")
public class FeedbackController {

    @Autowired
    private Service service;


    @GetMapping("/generate-feedback-excel")
    public void generateFeedbackExcel(HttpServletResponse response) throws IOException {

        // Generate the Excel file
        InputStream excelFile = service.generateExcelFile();

        // Set response headers
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=feedback_response_Records.xlsx");

        // Write the file to the response output stream
        try (OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = excelFile.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }

}

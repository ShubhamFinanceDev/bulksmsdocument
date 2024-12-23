package com.bulkSms.Controller;

import com.bulkSms.Model.RegistrationDetails;
import com.bulkSms.Model.SmsResponse;
import com.bulkSms.Service.Service;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.bulkSms.Model.CommonResponse;
import com.bulkSms.Service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.owasp.encoder.Encode;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("/admin")
public class Admin {
    @Autowired
    private Service service;

    // Helper function for sanitizing user input
    private String sanitizeInput(String input) {
        return Encode.forHtml(input); // OWASP Encoder to prevent XSS
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationDetails registerUserDetails) throws Exception {

        CommonResponse commonResponse = new CommonResponse();
        try {
            registerUserDetails.validate();
            service.registerNewUser(registerUserDetails);
            commonResponse.setMsg("User successfully registered!");
            return new ResponseEntity<>(commonResponse, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.error( e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
        catch (Exception e) {
            log.error( e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    @PostMapping("/csvUpload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") @NotNull MultipartFile file) {
        CommonResponse commonResponse = new CommonResponse();
        try {
            log.info("file upload job invoked");
            return ResponseEntity.ok(service.csvFileUploadSave(file).getBody());
        } catch (Exception e) {
            log.error( e.getMessage());
            commonResponse.setMsg("Technical issue : " + e.getMessage());
            return new ResponseEntity<>(commonResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/fetch-pdf")
    public ResponseEntity<?> pdfFetcherFromLocation(@RequestParam(name = "pdfUrl") @NotNull String pdfUrl ,
                                                    @RequestParam(name="category") @NotNull String category) throws IOException {
        // Sanitize user input to prevent XSS in logs or downstream processes
        pdfUrl = sanitizeInput(pdfUrl);
        category = sanitizeInput(category);
        service.fetchPdf(pdfUrl,category);
        return ResponseEntity.ok("Merged process invoked");
    }

    @GetMapping("/sms-process")
    public ResponseEntity<?> sendSms(@RequestParam(required = false) String smsCategory,@RequestParam String type,@RequestParam(defaultValue = "1") int pageNo) throws Exception
    {
        try {
            // Sanitize inputs
            type = sanitizeInput(type);
            if (smsCategory != null) {
                smsCategory = sanitizeInput(smsCategory);
            }
            switch (type) {
                case "new" :
                    log.info("Sms process invoked fro category {}", smsCategory);
                    return (service.sendSmsToUser(smsCategory));

                case "previous" :
                    return service.listOfSendSmsToUser(smsCategory,pageNo);

                case "unprocessed" :
                    return service.listOfUnsendSms(smsCategory,pageNo);

                default:
                    return new ResponseEntity<>("Invalid Type provided", HttpStatus.BAD_REQUEST);
            }
        }catch (Exception e) {
            log.error( e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    @GetMapping("/dashboard-view")
    public ResponseEntity<?> fetchDataForDashboard(@RequestParam(name = "pageNo",defaultValue = "1") int pageNo) throws Exception {
        try {
            log.info("Fetching dashboard data for page {}", pageNo);
            return service.getDashboardData(pageNo);
        }catch (Exception e){
            log.error("Error fetching dashboard data: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    @GetMapping("/get-job-audit-trail")
    public ResponseEntity<?> getJobAudit(){
        return service.returnJobAudit();
    }
}
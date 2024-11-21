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
            // Perform sanitization if needed (e.g., strings in the model)
            registerUserDetails.validate(); // Ensure internal validation checks are passed
            service.registerNewUser(registerUserDetails);
            commonResponse.setMsg("User successfully registered!");
            return new ResponseEntity<>(commonResponse, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    @PostMapping("/csvUpload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") @NotNull MultipartFile file) {
        CommonResponse commonResponse = new CommonResponse();
        try {
            log.info("File upload job invoked");
            return ResponseEntity.ok(service.csvFileUploadSave(file).getBody());
        } catch (Exception e) {
            log.error(e.getMessage());
            commonResponse.setMsg("Technical issue: " + Encode.forHtml(e.getMessage())); // Sanitize error message
            return new ResponseEntity<>(commonResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/fetch-pdf")
    public ResponseEntity<?> pdfFetcherFromLocation(@RequestParam(name = "pdfUrl") @NotNull String pdfUrl,
                                                    @RequestParam(name = "category") @NotNull String category) throws IOException {
        // Sanitize user input to prevent XSS in logs or downstream processes
        pdfUrl = sanitizeInput(pdfUrl);
        category = sanitizeInput(category);
        return service.fetchPdf(pdfUrl, category);
    }

    @GetMapping("/sms-process")
    public ResponseEntity<?> sendSms(@RequestParam(required = false) String smsCategory,
                                     @RequestParam @NotNull String type,
                                     @RequestParam(defaultValue = "1") int pageNo) throws Exception {
        try {
            // Sanitize inputs
            type = sanitizeInput(type);
            if (smsCategory != null) {
                smsCategory = sanitizeInput(smsCategory);
            }

            switch (type) {
                case "new":
                    log.info("SMS process invoked for category {}", smsCategory);
                    return service.sendSmsToUser(smsCategory);

                case "previous":
                    return service.listOfSendSmsToUser(smsCategory, pageNo);

                case "unprocessed":
                    return service.listOfUnsendSms(smsCategory, pageNo);

                default:
                    return new ResponseEntity<>("Invalid Type provided", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new Exception(Encode.forHtml(e.getMessage())); // Sanitize error message for logs
        }
    }

}
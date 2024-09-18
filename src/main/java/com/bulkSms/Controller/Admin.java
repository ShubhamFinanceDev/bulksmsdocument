package com.bulkSms.Controller;

import com.bulkSms.Model.RegistrationDetails;
import com.bulkSms.Model.SmsResponse;
import com.bulkSms.Service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.bulkSms.Model.CommonResponse;
import com.bulkSms.Service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class Admin {
    @Autowired
    private Service service;


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationDetails registerUserDetails) throws Exception {

        CommonResponse commonResponse = new CommonResponse();
        try {
            registerUserDetails.validate();
            service.registerNewUser(registerUserDetails);
            commonResponse.setMsg("User successfully registered!");
            return new ResponseEntity<>(commonResponse, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @PostMapping("/csvUpload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        CommonResponse commonResponse = new CommonResponse();
        try {
            return ResponseEntity.ok(service.save(file).getBody());
        } catch (Exception e) {
            commonResponse.setMsg("Technical issue : " + e.getMessage());
            return new ResponseEntity<>(commonResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/fetch-pdf")
    public ResponseEntity<?> pdfFetcherFromLocation(@RequestParam(name = "pdfUrl") String pdfUrl) throws IOException {
        return service.fetchPdf(pdfUrl);
    }

    @GetMapping("/sms-process")
    public ResponseEntity<?> sendSms(@RequestParam(required = false) String smsCategory,@RequestParam String type,@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) throws Exception
    {
        try {
            Pageable pageable = PageRequest.of(page,size);
            switch (type) {
                case "new" :
                    Page<Object> smsInformation = service.sendSmsToUser(smsCategory,pageable);
                    if (smsInformation.isEmpty()) {
                        SmsResponse response = new SmsResponse(0, "No unsent SMS found for category: " + smsCategory, smsInformation.getContent());
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    }
                    SmsResponse response = new SmsResponse(smsInformation.getNumberOfElements(), "success", smsInformation.getContent());
                    return new ResponseEntity<>(response, HttpStatus.OK);

                case "previous" :
                    Page<Object> smsInformation1 = service.ListOfSendSmsToUser(smsCategory,pageable);
                    SmsResponse response1 = new SmsResponse(smsInformation1.getNumberOfElements(), "success", smsInformation1.getContent());
                    return new ResponseEntity<>(response1, HttpStatus.OK);

                default:
                    SmsResponse response2 = new SmsResponse("Invalid Type provided");
                    return new ResponseEntity<>(response2, HttpStatus.BAD_REQUEST);
            }
        }catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

}
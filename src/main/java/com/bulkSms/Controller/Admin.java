package com.bulkSms.Controller;

import com.bulkSms.Model.RegistrationDetails;
import com.bulkSms.Model.SmsResponse;
import com.bulkSms.Service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.bulkSms.Model.CommonResponse;
import com.bulkSms.Service.Service;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<?> pdfFetcherFromLocation(@RequestParam(name = "pdfUrl",required = false) String pdfUrl,@RequestParam(name="category") String category,@RequestParam(name = "pageNo") int pageNo) throws IOException {
        return service.fetchPdf(pdfUrl,category, pageNo);
    }

    @GetMapping("/sms-process")
    public ResponseEntity<?> sendSms(@RequestParam(required = false) String smsCategory,@RequestParam String type,@RequestParam(defaultValue = "1") int pageNo) throws Exception
    {
        try {

            switch (type) {
                case "new" :
                    return (service.sendSmsToUser(smsCategory));

                case "previous" :
                    return service.listOfSendSmsToUser(smsCategory,pageNo);

                case "unprocessed" :
                    return service.listOfUnsendSms(smsCategory,pageNo);

                default:
                    return new ResponseEntity<>("Invalid Type provided", HttpStatus.BAD_REQUEST);
            }
        }catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

}
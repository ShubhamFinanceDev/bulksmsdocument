package com.bulkSms.Controller;

import com.bulkSms.Model.RegistrationDetails;
import com.bulkSms.Service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.bulkSms.Model.CommonResponse;
import com.bulkSms.Service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin")
public class Admin {
    @Autowired
    private Service service;

    @PostMapping("/message")
    public ResponseEntity<String> postMessage() {
        return ResponseEntity.ok("Message received by admin successfully!");
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
            throw new IllegalArgumentException(e.getMessage());
        }
        catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

}
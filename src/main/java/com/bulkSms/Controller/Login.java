package com.bulkSms.Controller;

import com.bulkSms.Entity.FeedbackRecord;
import com.bulkSms.Entity.UserFeedbackResponse;
import com.bulkSms.JwtAuthentication.JwtHelper;
import com.bulkSms.Model.CommonResponse;
import com.bulkSms.Model.JwtRequest;
import com.bulkSms.Model.JwtResponse;
import com.bulkSms.Model.RegistrationDetails;
import com.bulkSms.Service.Service;
import com.bulkSms.Utility.EncodingUtils;
import com.bulkSms.Utility.FeedbackUtility;
import jakarta.validation.Valid;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/sms-service")
public class Login {

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private AuthenticationManager manager;
    @Autowired
    private Service service;
    @Autowired
    private EncodingUtils encodingUtils;

    @Autowired
    private JwtHelper helper;
    @Autowired
    private FeedbackUtility feedbackUtility;

    Logger logger = LoggerFactory.getLogger(Login.class);

    private String sanitizeInput(String input) {
        return Encode.forHtml(input);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody @Valid JwtRequest request) {
        final int[] userRole = new int[1];
        List<String> roleList = new ArrayList<>();
        String sanitizedEmail = sanitizeInput(request.getEmailId());
        String sanitizedPassword = sanitizeInput(request.getPassword());

        UserDetails userDetails = userDetailsService.loadUserByUsername(sanitizedEmail);

        this.doAuthenticate(sanitizedEmail, sanitizedPassword);

        String token = this.helper.generateToken(userDetails);
        userDetails.getAuthorities().forEach(grantedAuthority -> {
            String roleName = String.valueOf(grantedAuthority);
            roleList.add(roleName);
        });

        if (roleList.contains("ROLE_ADMIN")) {
            userRole[0] = 0;
        } else if (roleList.contains("ROLE_USER")) {
            userRole[0] = 1;
        }

        JwtResponse response = JwtResponse.builder()
                .token(token).role(userRole[0])
                .emailId(userDetails.getUsername()).build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void doAuthenticate(String email, String password) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, password);
        try {
            manager.authenticate(authentication);


        } catch (BadCredentialsException e) {
            throw new BadCredentialsException(" Invalid Username or Password  !!");
        }
    }

//    @GetMapping("/download-pdf/{category}/{loanNo}")
//    public ResponseEntity<?> downloadPdfFile(@PathVariable("category") String category, @PathVariable("loanNo") String loanNo) {
//        CommonResponse commonResponse = new CommonResponse();
//        String loanNoDecoded = encodingUtils.decode(loanNo);
//        try {
//            return service.fetchPdfFileForDownload(loanNoDecoded,category);
//        }catch (Exception e){
//            commonResponse.setMsg("Exception :" +e.getMessage());
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }


    @GetMapping("/download-kit/{category}/{loanNo}")
    public ResponseEntity<byte[]> downloadPdfFileBySmsLink(@PathVariable("category") String category, @PathVariable("loanNo") String loanNo){
        try {
//            String decodedLoanNo = encodingUtils.decode(loanNo);
            logger.info("request for pdf download encrypted {}",loanNo);
            return service.fetchPdfFileForDownloadBySmsLink(loanNo, category);
        }catch (Exception e){
            System.out.println("Exception found :"+e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @GetMapping("/send-feedback-sms")
//
//      public  String sendSms(@RequestParam String mobileNumber,@RequestParam long formId)
//    {
//        feedbackUtility.getFeedBack(mobileNumber,formId);
//        return "success";
//    }


}

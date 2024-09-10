package com.bulkSms.Controller;

import com.bulkSms.JwtAuthentication.JwtHelper;
import com.bulkSms.Model.CommonResponse;
import com.bulkSms.Model.JwtRequest;
import com.bulkSms.Model.JwtResponse;
import com.bulkSms.Service.Service;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
    private JwtHelper helper;

    Logger logger = LoggerFactory.getLogger(Login.class);


    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest request) {


        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmailId());

        this.doAuthenticate(request.getEmailId(), request.getPassword());

        String token = this.helper.generateToken(userDetails);

        JwtResponse response = JwtResponse.builder()
                .token(token)
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

    @ExceptionHandler(BadCredentialsException.class)
    public String exceptionHandler() {
        return "Credentials Invalid !!";
    }

    @GetMapping("/fetch-pdf")
    public ResponseEntity<?> pdfFetcherFromLocation(@RequestParam(name = "pdfUrl") String pdfUrl) throws IOException {
        return service.fetchPdf(pdfUrl);
    }
}

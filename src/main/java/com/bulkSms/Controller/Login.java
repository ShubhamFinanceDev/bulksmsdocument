package com.bulkSms.Controller;

import com.bulkSms.JwtAuthentication.JwtHelper;
import com.bulkSms.Model.CommonResponse;
import com.bulkSms.Model.JwtRequest;
import com.bulkSms.Model.JwtResponse;
import com.bulkSms.Model.RegistrationDetails;
import com.bulkSms.Service.Service;
import com.bulkSms.Utility.EncodingUtils;
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
import org.springframework.validation.BindingResult;
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
    private EncodingUtils encodingUtils;

    @Autowired
    private JwtHelper helper;

    Logger logger = LoggerFactory.getLogger(Login.class);

    private String sanitizeInput(String input) {
        return Encode.forHtml(input);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody @Valid JwtRequest request) {

        String sanitizedEmail = sanitizeInput(request.getEmailId());
        String sanitizedPassword = sanitizeInput(request.getPassword());

        UserDetails userDetails = userDetailsService.loadUserByUsername(sanitizedEmail);

        this.doAuthenticate(sanitizedEmail, sanitizedPassword);

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
            throw new BadCredentialsException("Invalid Username or Password!");
        }
    }

    @GetMapping("/dashboard-view")
    public ResponseEntity<?> fetchDataForDashboard(@RequestParam(name = "pageNo", defaultValue = "1") int pageNo) throws Exception {
        try {
            logger.info("Fetching dashboard data for page {}", pageNo);
            return service.getDashboardData(pageNo);
        } catch (Exception e) {
            logger.error("Error fetching dashboard data: {}", e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    @GetMapping("/download-kit/{category}/{loanNo}")
    public ResponseEntity<byte[]> downloadPdfFileBySmsLink(@PathVariable("category") String category, @PathVariable("loanNo") String loanNo) {
        try {
            String decodedLoanNo = encodingUtils.decode(sanitizeInput(loanNo));
            logger.info("Request for PDF download, category: {}, loanNo: {}", category, decodedLoanNo);
            return service.fetchPdfFileForDownloadBySmsLink(decodedLoanNo, category);
        } catch (Exception e) {
            logger.error("Exception while downloading PDF: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

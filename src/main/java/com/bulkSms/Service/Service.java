package com.bulkSms.Service;

import com.bulkSms.Model.CommonResponse;
import com.bulkSms.Model.RegistrationDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;

public interface Service {
    ResponseEntity<?> fetchPdf(String pdfUrl) throws IOException;
    ResponseEntity<CommonResponse> save(MultipartFile file) throws Exception;

    void registerNewUser(RegistrationDetails registerUserDetails) throws Exception;

    Page<Object> sendSmsToUser(String smsCategory, Pageable pageable) throws Exception;

    Page<Object> listOfSendSmsToUser(String smsCategory, Pageable pageable) throws Exception;
}

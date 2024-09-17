package com.bulkSms.Service;

import com.bulkSms.Model.CommonResponse;
import com.bulkSms.Model.RegistrationDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;

public interface Service {
    ResponseEntity<?> fetchPdf(String pdfUrl) throws IOException;
    ResponseEntity<CommonResponse> save(MultipartFile file) throws Exception;

    void registerNewUser(RegistrationDetails registerUserDetails) throws Exception;

    List<Object> sendSmsToUser(String smsCategory) throws Exception;

    List<Object> ListOfSendSmsToUser(String smsCategory) throws Exception;
}

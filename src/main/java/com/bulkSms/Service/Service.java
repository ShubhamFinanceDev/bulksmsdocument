package com.bulkSms.Service;

import com.bulkSms.Model.CommonResponse;
import com.bulkSms.Model.RegistrationDetails;
import com.bulkSms.Model.SmsResponse;
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

    SmsResponse sendSmsToUser(String smsCategory, int pageNo) throws Exception;

    SmsResponse listOfSendSmsToUser(String smsCategory, int pageNo) throws Exception;

    SmsResponse listOfUnsendSms(String smsCategory, int pageNo) throws Exception;
}

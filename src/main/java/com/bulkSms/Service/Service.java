package com.bulkSms.Service;

import com.bulkSms.Model.CommonResponse;
import com.bulkSms.Model.RegistrationDetails;
import com.bulkSms.Model.SmsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;

public interface Service {
    ResponseEntity<?> fetchPdf(String pdfUrl) throws IOException;

    List<Object> save(MultipartFile file) throws Exception;

    void registerNewUser(RegistrationDetails registerUserDetails) throws Exception;

    ResponseEntity<?> fetchPdfFileForDownload(String loanNo) throws Exception;

    SmsResponse sendSmsToUser(String smsCategory) throws Exception;

    SmsResponse listOfSendSmsToUser(String smsCategory, int pageNo) throws Exception;

    ResponseEntity<?> getDashboardData() throws Exception;

    ResponseEntity<?> fetchPdfFileForDownloadBySmsLink(String loanNo);

    SmsResponse listOfUnsendSms(String smsCategory, int pageNo) throws Exception;
}

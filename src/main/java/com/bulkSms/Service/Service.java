package com.bulkSms.Service;

import com.bulkSms.Model.CommonResponse;
import com.bulkSms.Model.RegistrationDetails;
import com.bulkSms.Model.SmsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;

public interface Service {
    ResponseEntity<?> fetchPdf(String pdfUrl,String category) throws IOException;

    ResponseEntity<CommonResponse> save(MultipartFile file) throws Exception;

    void registerNewUser(RegistrationDetails registerUserDetails) throws Exception;

//    ResponseEntity<?> fetchPdfFileForDownload(String loanNo,String category) throws Exception;

   ResponseEntity<?> sendSmsToUser(String smsCategory) throws Exception;

    ResponseEntity<?> listOfSendSmsToUser(String smsCategory, int pageNo) throws Exception;

    ResponseEntity<?> getDashboardData() throws Exception;

    ResponseEntity<byte[]> fetchPdfFileForDownloadBySmsLink(String loanNo,String category) throws Exception;

    ResponseEntity<?> listOfUnsendSms(String smsCategory, int pageNo) throws Exception;
}

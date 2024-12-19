package com.bulkSms.Service;

import com.bulkSms.Entity.FeedbackRecord;
import com.bulkSms.Entity.UserFeedbackResponse;
import com.bulkSms.Model.CommonResponse;
import com.bulkSms.Model.RegistrationDetails;
import com.bulkSms.Model.SmsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

public interface Service {
    ResponseEntity<?> fetchPdf(String pdfUrl,String category) throws IOException;

    ResponseEntity<CommonResponse> csvFileUploadSave(MultipartFile file) throws Exception;

    void registerNewUser(RegistrationDetails registerUserDetails) throws Exception;

//    ResponseEntity<?> fetchPdfFileForDownload(String loanNo,String category) throws Exception;

   ResponseEntity<?> sendSmsToUser(String smsCategory) throws Exception;

    ResponseEntity<?> listOfSendSmsToUser(String smsCategory, int pageNo) throws Exception;

    ResponseEntity<?> getDashboardData(int pageNo) throws Exception;

    ResponseEntity<byte[]> fetchPdfFileForDownloadBySmsLink(String loanNo,String category) throws Exception;

    ResponseEntity<?> listOfUnsendSms(String smsCategory, int pageNo) throws Exception;

    FeedbackRecord getFeedbackRecord(String formId);

    void submitFeedback(String formId, UserFeedbackResponse feedback);

    InputStream generateExcelFile();
}

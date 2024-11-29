package com.bulkSms.Utility;


import com.bulkSms.Entity.FeedbackRecord;
import com.bulkSms.Model.GetDataForSendSms;
import com.bulkSms.Model.FeedbackResponse;

import com.bulkSms.Repository.FeedbackRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class FeedbackUtility {

    @Value("${feedback.url}")
    private String feedbackUrl;

    @Value("${feedback.key}")
    private String apiKey;

    @Value("${feedback.formname}")
    private String formName;

    @Value("${feedback.operation}")
    private String operation;

    @Value("${feedback.pageno}")
    private String pageNo;

    @Value("${feedback.numofrecords}")
    private String numOfRecords;

    @Value("${feedback.sortcolumn}")
    private String sortColumn;

    @Value("${feedback.sortorder}")
    private String sortOrder;

    @Value("${feedback.isnull}")
    private String isNull;

    @Value("${feedback.templatename}")
    private String templateName;

    @Value("${feedback.searchcondition}")
    private String searchCondition;

    @Autowired
    private FeedbackRepo feedbackRepo;

    @Autowired
    private SmsUtility smsUtility;


//        @Scheduled(cron = "0 * * * * *")
    public FeedbackResponse getFeedBack(String mobileNo,Long formId) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        try {
            String url = String.format("%s?apikey=%s&formname=%s&operation=%s&pageno=%s&numofrecords=%s&sortcolumn=%s&sortorder=%s&isnull=%s&templatename=%s&searchcondition=%s",
                    feedbackUrl, URLDecoder.decode(apiKey, StandardCharsets.UTF_8), formName, operation, pageNo, numOfRecords,
                    URLDecoder.decode(sortColumn, StandardCharsets.UTF_8), sortOrder, isNull,
                    URLDecoder.decode(templateName, StandardCharsets.UTF_8), URLDecoder.decode(searchCondition, StandardCharsets.UTF_8));

            log.info("Request URL: {}", url);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String responseBody = response.getBody();
                log.info("Raw response data: {}", responseBody);

                // Parse JSON to Java object
                ObjectMapper objectMapper = new ObjectMapper();
                FeedbackResponse feedbackResponse = objectMapper.readValue(responseBody, FeedbackResponse.class);

                // Save each entry in the database
//                for (Map<String, Object> entry : feedbackResponse.getData()) {
//                    String formId = (String) entry.get("Form ID");
//                    String contactNo = (String) entry.get("Contact No 1");
//
//                    if (formId != null && contactNo != null) {
//                        //send feedback message including link
////                        smsUtility.sendFeedbackFormToUser(formId,contactNo);
//                        FeedbackRecord feedbackRecord = new FeedbackRecord();
//                        feedbackRecord.setFormId(formId);
//                        feedbackRecord.setContactNo(contactNo);
//                        feedbackRecord.setFeedbackSendFlag("Y");
//                        feedbackRepo.save(feedbackRecord);
//                    }
//                }
//                long formId = 745935;
                String contactNo = mobileNo;
                String loanNo = "APPLl000123";
                String customerName = "Nainish SIngh";
                GetDataForSendSms data = new GetDataForSendSms();
                data.setLoanNumber(loanNo);
                data.setCertificateCategory("feedback");
                data.setMobileNumber(contactNo);
                data.setFileSequenceNo(formId);
                FeedbackRecord feedbackRecord = new FeedbackRecord();
                feedbackRecord.setFormId(formId);
                feedbackRecord.setContactNo(contactNo);
                feedbackRecord.setFeedbackSendFlag("Y");
                feedbackRecord.setFeedbackSubmitFlag("N");
                feedbackRecord.setLoanNo(loanNo);
                feedbackRecord.setCustomerName(customerName);
                feedbackRepo.save(feedbackRecord);
                smsUtility.sendTextMsgToUser(data);
                log.info("Data saved to database successfully.");
                return feedbackResponse;
            } else {
                log.error("Unexpected response status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error fetching feedback data: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching feedback data", e);
        }
        return null;
    }


}


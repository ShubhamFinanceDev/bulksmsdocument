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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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


    @Scheduled(cron = "0 0/30 10-17 * * *")
    public FeedbackResponse getFeedBack() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        List<FeedbackRecord> feedbackRecords = new ArrayList<>();

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

                String size = String.valueOf(feedbackResponse.getResponserows());

//                // Save each entry in the database
                for (Map<String, Object> entry : feedbackResponse.getData()) {
                    String formId = (String) entry.get("Form ID");
                    String contactNo = (String) entry.get("Contact No 1");
                    String loanNo = (String) entry.get("Application No");
                    String customerFirstName = (String) entry.get("First Name");

                    if (formId != null && contactNo != null && !loanNo.equalsIgnoreCase("NA")) {
                        boolean formIdExists = feedbackRepo.existsByFormId(formId);

                        if (!formIdExists) {

                            FeedbackRecord feedbackRecord = new FeedbackRecord();
                            feedbackRecord.setFormId(Long.valueOf(formId));
                            feedbackRecord.setContactNo(contactNo);
                            feedbackRecord.setLoanNo(loanNo);
                            feedbackRecord.setCustomerName(customerFirstName);
                            feedbackRecord.setFeedbackSendFlag("Y");
                            feedbackRecord.setFeedbackSubmitFlag("N");
                            feedbackRepo.save(feedbackRecord);
                            feedbackRecords.add(feedbackRecord);

//                            if you want to use the feedback sms functionality then uncomment from line 121 to 127


//                            GetDataForSendSms data = new GetDataForSendSms();
//                            data.setLoanNumber(loanNo);
//                            data.setCertificateCategory("feedback");
//                            data.setMobileNumber(contactNo);
//                            data.setFileSequenceNo(formId);

//                          smsUtility.sendTextMsgToUser(data);
                        } else {
                            log.info("This Form ID already exists: " + formId);
                        }
                    }

                }
                log.info("total data size from 3rd party api : {}", size);
                log.info("size of records saved in db : {}", feedbackRecords.size());
                log.info("records that are saved in db : {}", feedbackRecords );

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


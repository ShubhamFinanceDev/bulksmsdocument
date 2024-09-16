package com.bulkSms.Utility;

import com.bulkSms.Entity.DataUpload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Slf4j
@Component
public class SmsUtility {

    @Value("${sms.url}")
    private String smsUrl;

    @Value("${sms.method}")
    private String smsMethod;

    @Value("${sms.key}")
    private String smsKey;

    @Value("${sms.format}")
    private String smsFormat;

    @Value("${sms.sender}")
    private String smsSender;


    public void sendTextMsgToUser(DataUpload smsSendDetails) {
        String mobileNumber = smsSendDetails.getMobileNumber();

        String url = smsUrl + "?method=" + smsMethod + "&api_key=" + smsKey + "&to=" + mobileNumber +
                "&sender=" + smsSender + "&message=" + SmsTemplate.adhocMessage+smsSendDetails.getLoanNumber() + "&format=" + smsFormat + "&unicode=auto";

        System.out.println("Constructed URL: " + url);

        try {
            RestTemplate restTemplate = new RestTemplate();
            HashMap<String, String> otpResponse = restTemplate.getForObject(url, HashMap.class);

            if (otpResponse != null && "OK".equals(otpResponse.get("status"))) {
                log.info("SMS Sent Successfully to {}", mobileNumber);
            } else {
                log.error("Failed to send SMS to {}: {}", mobileNumber, otpResponse);
            }
        } catch (Exception e) {
            log.error("Error while sending SMS to {}: {}", mobileNumber, e.getMessage(), e);
        }
    }
}


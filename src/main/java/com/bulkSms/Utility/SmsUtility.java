package com.bulkSms.Utility;

import com.bulkSms.Entity.DataUpload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Slf4j
@Component
public class SmsUtility {
    @Autowired
    private EncodingUtils encodingUtils;

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
    @Value("${kit.url}")
    private String kitBaseurl;


    public void sendTextMsgToUser(DataUpload smsSendDetails) throws Exception{
        String mobileNumber = smsSendDetails.getMobileNumber();
        String kitUrl=SmsTemplate.adhocMessage + kitBaseurl+encodingUtils.encode(smsSendDetails.getLoanNumber());
        String url = smsUrl + "?method=" + smsMethod + "&api_key=" + smsKey + "&to=" + mobileNumber +
                "&sender=" + smsSender + "&message=" + kitUrl + "&format=" + smsFormat + "&unicode=auto";

            RestTemplate restTemplate = new RestTemplate();
            HashMap<String, String> otpResponse = restTemplate.getForObject(url, HashMap.class);

            if (otpResponse != null && "OK".equals(otpResponse.get("status"))) {
                log.info("SMS Sent Successfully to {}", mobileNumber);
            } else {
                log.error("Failed to send SMS to {}: {}", mobileNumber, otpResponse);
            }
    }
}


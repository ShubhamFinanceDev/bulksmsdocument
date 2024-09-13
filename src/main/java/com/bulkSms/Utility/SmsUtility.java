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

    public String message = "Dear+Customer,+Congratulations+to+be+part+of+the+Shubham+family,+we+are+pleased+to+share+your+welcome+kit+having+welcome+letter,+repayment+schedule+%26+sanction+letter+cum+MITC.+Kindly+download+your+welcome+kit+from+below+link.+For+any+enquiry+related+to+this,+you+can+call+at+our+customer+care+toll+free+no.+-+1800+258+2225+or+email+at+customercare@shubham.co%0aLink:-https://docs.shubham.co:8443/BulkSMS/downloadadhocCertificate/TGOXfWuqvIz6LaSjyHpGbn7lnrKUa4dScQeBbmSl0mwoJYcbrh";

    public void sendTextMsgToUser(DataUpload smsSendDetails) {
        String mobileNumber = smsSendDetails.getMobileNumber();

        String url = smsUrl + "?method=" + smsMethod + "&api_key=" + smsKey + "&to=" + mobileNumber +
                "&sender=" + smsSender + "&message=" + message + "&format=" + smsFormat + "&unicode=auto";

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


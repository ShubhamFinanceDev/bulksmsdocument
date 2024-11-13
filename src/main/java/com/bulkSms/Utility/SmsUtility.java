package com.bulkSms.Utility;

import com.bulkSms.Entity.DataUpload;
import com.bulkSms.Model.GetDataForSendSms;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Year;
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

    int year = Year.now().getValue();

    private String adhocMessage = "Dear%20Customer%2C%20Congratulations%20to%20be%20part%20of%20the%20Shubham%20family%2C%20we%20are%20pleased%20to%20share%20your%20welcome%20kit%20having%20welcome%20letter%2C%20repayment%20schedule%20%26%20sanction%20letter%20cum%20MITC.%20Kindly%20download%20your%20welcome%20kit%2C%20fair%20practice%20code%2C%20%26%20exclusion%20list%20from%20below%20links.%20For%20any%20enquiry%20related%20to%20this%2C%20you%20can%20call%20at%20our%20customer%20care%20toll%20free%20no.%20-%201800%20258%202225%20or%20email%20at%20customercare%40shubham.co%0AWelcome%20kit%20Link%3A- ";
    private String soaTemplate = "Dear+Customer,%0aPlease+download+your+Yearly+Statement+of+Account+from+below+link+for+the+period+of+01-Apr-" + (year - 1) + "+to+31-Mar-" + year + ".%0aRegards%0aShubham+Housing+Development+Finance+Company+Ltd%0aLink: ";
    private String interestCertificateTemplate = "Dear+Customer,%0aPlease+download+your+Yearly+Interest+Certificate+from+below+link+for+the+period+of+01-Apr-" + (year - 1) + "+to+31-Mar-" + year + ".%0aRegards%0aShubham+Housing+Development+Finance+Company+Ltd%0aLink: ";
    private String reminderPayment = "";
    private String soaQuarterly = "Dear Customer, Please download your Quarterly Statement of Account from below link for the period of Jul’24 to Sep’24.\n" +
            "Regards\n" +
            "Shubham Housing Development Finance Company Ltd\n" +
            "Link: ";


    @Async
    public void sendTextMsgToUser(GetDataForSendSms smsSendDetails) throws Exception {
        String mobileNumber = smsSendDetails.getMobileNumber();
        String key = "/" + smsSendDetails.getCertificateCategory() + "/" +smsSendDetails.getLoanNumber();
        String smsBody = makeSmsCustomBody(smsSendDetails, key);


        if (smsBody!= null) {

            String  url = smsUrl + "?method=" + smsMethod + "&api_key=" + smsKey + "&to=" + mobileNumber +
                    "&sender=" + smsSender + "&message=" + smsBody + "&format=" + smsFormat + "&unicode=auto"+"&shortUrl=1";

            RestTemplate restTemplate = new RestTemplate();
            HashMap<String, String> otpResponse = restTemplate.getForObject(url, HashMap.class);

            if (otpResponse != null && "OK".equals(otpResponse.get("status"))) {
                log.info("SMS Sent Successfully to {}", mobileNumber);
                System.out.println(smsBody);
            } else {
                log.error("Failed to send SMS to {}: {}", mobileNumber, otpResponse);
            }
        } else {
            log.info("SMS template no fround for category {}", smsSendDetails.getCertificateCategory());
        }
    }


    private String makeSmsCustomBody(GetDataForSendSms smsSendDetails, String key) {
        String smsBody=null;

        if (smsSendDetails.getCertificateCategory().equals("ADHOC")) {
            smsBody = adhocMessage + kitBaseurl + key;
            smsBody += "\nFPC link:- https://shubham.co/policies/fair-practice-code\n" +
                    "Exclusion list link:- https://bit.ly/SHDFCEL";

        } else if (smsSendDetails.getCertificateCategory().equals("SOA")) {
            smsBody = soaTemplate + kitBaseurl + key+"@"+smsSendDetails.getFileSequenceNo();

        } else if (smsSendDetails.getCertificateCategory().equals("INTEREST_CERTIFICATE")) {

            smsBody = interestCertificateTemplate + kitBaseurl + key+"@"+smsSendDetails.getFileSequenceNo();
        } else if (smsSendDetails.getCertificateCategory().equals("SOA_QUARTERLY")) {
            smsBody=  soaQuarterly + kitBaseurl + key+"@"+smsSendDetails.getFileSequenceNo();

        }
        return smsBody;
    }


}


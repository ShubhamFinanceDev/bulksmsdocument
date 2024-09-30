package com.bulkSms.Utility;

import com.bulkSms.Entity.DataUpload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    private   String adhocMessage = "Dear+Customer,+Congratulations+to+be+part+of+the+Shubham+family,+we+are+pleased+to+share+your+welcome+kit+having+welcome+letter,+repayment+schedule+%26+sanction+letter+cum+MITC.+Kindly+download+your+welcome+kit+from+below+link.+For+any+enquiry+related+to+this,+you+can+call+at+our+customer+care+toll+free+no.+-+1800+258+2225+or+email+at+customercare@shubham.co%0aLink:-";
   private String soaTemplate="Dear+Customer,%0aPlease+download+your+Yearly+Statement+of+Account+from+below+link+for+the+period+of+01-Apr-"+(year-1)+"+to+31-Mar-"+year+".%0aRegards%0aShubham+Housing+Development+Finance+Company+Ltd%0aLink:";
   private String interestCertificateTemplate="Dear+Customer,%0aPlease+download+your+Yearly+Interest+Certificate+from+below+link+for+the+period+of+01-Apr-"+(year-1)+"+to+31-Mar-"+year+".%0aRegards%0aShubham+Housing+Development+Finance+Company+Ltd%0aLink:";
   private String reminderPayment="";
    public  String buildSmsBody(String lonNo, String category)
    {
        String key="/"+category+"/"+lonNo;
        String url="";
        switch (category) {
            case "ADHOC":
                url+= adhocMessage+ kitBaseurl + key;
                url+="\nFPC link:- https://shubham.co/policies/fair-practice-code +\n\n" +
                        "Regards\n" +
                        "Shubham Housing";
                break;
            case "INTEREST_CERTIFICATE":
                url+= interestCertificateTemplate+kitBaseurl + key;
                break;
            case "SOA":
                url+= soaTemplate+kitBaseurl + key;
                break;
            case "reminderPayment":
             url+=reminderPayment+key;
            default:
                url=null;
        }
        System.out.println(url);
     return url;
    }

    public void sendTextMsgToUser(DataUpload smsSendDetails) throws Exception{

            String smsBody=buildSmsBody(encodingUtils.encode(smsSendDetails.getLoanNumber()),smsSendDetails.getCertificateCategory());

            if(smsBody.length()>0 && smsBody!=null) {

                String mobileNumber = smsSendDetails.getMobileNumber();
//                String kitUrl=SmsTemplate.adhocMessage + kitBaseurl+encodingUtils.encode(smsSendDetails.getLoanNumber());
                String url = smsUrl + "?method=" + smsMethod + "&api_key=" + smsKey + "&to=" + mobileNumber +
                        "&sender=" + smsSender + "&message=" + smsBody + "&format=" + smsFormat + "&unicode=auto";

                RestTemplate restTemplate = new RestTemplate();
                HashMap<String, String> otpResponse = restTemplate.getForObject(url, HashMap.class);

                if (otpResponse != null && "OK".equals(otpResponse.get("status"))) {
                    log.info("SMS Sent Successfully to {}", mobileNumber);
                } else {
                    log.error("Failed to send SMS to {}: {}", mobileNumber, otpResponse);
                }
            }
            else {

                log.info("SMS template no fround for category {}",smsSendDetails.getCertificateCategory());
            }
    }
}


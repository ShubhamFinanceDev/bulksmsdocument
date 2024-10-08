package com.bulkSms.Utility;

import java.time.Year;

public class SmsTemplate {

    public static String adhocMessage = "Dear+Customer,+Congratulations+to+be+part+of+the+Shubham+family,+we+are+pleased+to+share+your+welcome+kit+having+welcome+letter,+repayment+schedule+%26+sanction+letter+cum+MITC.+Kindly+download+your+welcome+kit+from+below+link.+For+any+enquiry+related+to+this,+you+can+call+at+our+customer+care+toll+free+no.+-+1800+258+2225+or+email+at+customercare@shubham.co%0aLink:-";
    public  String soaTemplate(String lonNo, String category)
    {
        int year = Year.now().getValue();
        String url="Dear+Customer,%0aPlease+download+your+Yearly+Statement+of+Account+from+below+link+for+the+period+of+01-Apr-"+(year-1)+"+to+31-Mar-"+year+".%0aRegards%0aShubham+Housing+Development+Finance+Company+Ltd%0aLink:";

        return url;
    }
}

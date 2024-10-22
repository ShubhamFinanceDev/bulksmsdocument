package com.bulkSms.Utility;

import org.springframework.stereotype.Component;

import java.util.Base64;
@Component
public class EncodingUtils {

    public String encode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes())+"shubh";
    }

    public String decode(String input) {
        String subString=input.substring(input.length()-4);
        return new String(Base64.getDecoder().decode(subString));
    }
}

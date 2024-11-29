package com.bulkSms.Model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class JwtResponse {
    private String token;
    private String emailId;
    private int role;
}

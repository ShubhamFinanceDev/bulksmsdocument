package com.bulkSms.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FeedbackResponse {
    private int pageno;
    private int totalrows;
    private int responserows;

    @JsonProperty("data")
    private List<Map<String, Object>> data;


}

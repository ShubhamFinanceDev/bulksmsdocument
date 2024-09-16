package com.bulkSms.Utility;
import com.bulkSms.Entity.BulkSms;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvFileUtility{
    public static String TYPE = "text/csv";

    public boolean hasCsvFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    public List<BulkSms> csvBulksms(InputStream inputStream) throws Exception {
        List<BulkSms> bulkSmsList = new ArrayList<>();

        try (BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
             CSVParser csvParser = new CSVParser(bReader, CSVFormat.DEFAULT.withDelimiter('|').withTrim())) {
            List<CSVRecord> csvRecords = csvParser.getRecords();
            for (CSVRecord record : csvRecords) {
                BulkSms bulkSms = new BulkSms();
                bulkSms.setLoanNumber(record.get(0));
                bulkSms.setMobileNumber(record.get(1));
                bulkSms.setCertificateCategory(record.get(2));
                bulkSmsList.add(bulkSms);
            }
        }
        return bulkSmsList;
    }
}
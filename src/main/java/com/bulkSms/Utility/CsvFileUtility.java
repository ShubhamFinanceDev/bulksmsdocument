package com.bulkSms.Utility;
import com.bulkSms.Entity.BulkSms;
import com.bulkSms.Entity.DataUpload;
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

    public List<DataUpload> csvBulksms(InputStream inputStream) throws Exception {
        List<DataUpload> dataUploadList = new ArrayList<>();

        try (BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
             CSVParser csvParser = new CSVParser(bReader, CSVFormat.DEFAULT.withDelimiter('|').withTrim())) {
            List<CSVRecord> csvRecords = csvParser.getRecords();
            for (CSVRecord record : csvRecords) {
                DataUpload dataUpload = new DataUpload();
                BulkSms bulkSms=new BulkSms();
                dataUpload.setLoanNumber(record.get(0));
                dataUpload.setMobileNumber(record.get(1));
                dataUpload.setSmsFlag("N");
                dataUpload.setCertificateCategory(record.get(2));
                bulkSms.setSmsTimeStamp(null);
                bulkSms.setDataUpload(dataUpload);
                dataUpload.setBulkSms(bulkSms);
                dataUploadList.add(dataUpload);
            }
        }
        return dataUploadList;
    }
}
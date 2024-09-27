package com.bulkSms.Utility;
import com.bulkSms.Entity.BulkSms;
import com.bulkSms.Entity.DataUpload;
import com.bulkSms.Repository.DataUploadRepo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CsvFileUtility {
    public static String TYPE = "text/csv";

    @Autowired
    private DataUploadRepo dataUploadRepo;

    public boolean hasCsvFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    public List<DataUpload> csvBulksms(InputStream inputStream) throws Exception {
        List<DataUpload> dataUploadList = new ArrayList<>();

        try (BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
             CSVParser csvParser = new CSVParser(bReader, CSVFormat.DEFAULT.withDelimiter('|').withTrim())) {
            List<CSVRecord> csvRecords = csvParser.getRecords();
            for (CSVRecord record : csvRecords) {
                String loanNumber = record.get(0);
                Optional<DataUpload> existingDataUpload = dataUploadRepo.findByloanNumber(loanNumber);

                if (existingDataUpload.isPresent()) {
                    DataUpload dataUpload = existingDataUpload.get();
                    dataUpload.setSmsFlag("N");
                    dataUploadRepo.save(dataUpload);
                } else {
                    DataUpload dataUpload = new DataUpload();
                    BulkSms bulkSms = new BulkSms();
                    dataUpload.setLoanNumber(record.get(0));
                    dataUpload.setMobileNumber(record.get(1));
                    dataUpload.setCertificateCategory(record.get(2));
                    dataUpload.setSmsFlag(record.get(3));
                    bulkSms.setSmsTimeStamp(null);
                    bulkSms.setDataUpload(dataUpload);
                    dataUploadList.add(dataUpload);
                }
            }
            return dataUploadList;
        }
    }
}
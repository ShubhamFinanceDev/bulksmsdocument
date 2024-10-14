package com.bulkSms.Utility;

import com.bulkSms.Entity.BulkSms;
import com.bulkSms.Entity.DataUpload;
import com.bulkSms.Model.CommonResponse;
import com.bulkSms.Repository.DataUploadRepo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Component
public class CsvFileUtility {
    public static String TYPE = "text/csv";

    @Autowired
    private DataUploadRepo dataUploadRepo;

    public boolean hasCsvFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }
    @Transactional
    public void readCsvFile(InputStream inputStream) throws Exception {
        List<DataUpload> dataUploadList = new ArrayList<>();
        Set<String> uniqueRecords = new HashSet<>();  // To store unique key combinations

        try (BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8")); CSVParser csvParser = new CSVParser(bReader, CSVFormat.DEFAULT.withDelimiter('|').withTrim())) {

            for (CSVRecord record : csvParser) {
                String loanNumber = record.get(0);
                String certificateCategory = record.get(2);

                // Combine loanNumber and certificateCategory to create a unique key
                String uniqueKey = loanNumber + "|" + certificateCategory;

                if (uniqueRecords.contains(uniqueKey)) {
                    // Skip this record if it's already in the Set
                    System.out.println("Duplicate found for LoanNumber: " + loanNumber + ", CertificateCategory: " + certificateCategory);
                    continue;
                } else {
                    // Add the unique key to the Set
                    uniqueRecords.add(uniqueKey);
                }

                // Check for existing data in the database
                Optional<DataUpload> existingDataUpload = dataUploadRepo.findByloanNumber(loanNumber, certificateCategory);

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
                    dataUpload.setSmsFlag("N");

                    bulkSms.setSmsTimeStamp(null);
                    bulkSms.setDataUpload(dataUpload);
                    dataUpload.setBulkSms(bulkSms);

                    dataUploadList.add(dataUpload);
                }

                // Optional: Save records in batches
                if (dataUploadList.size() >= 5000) {  // Process every 100 records
                    dataUploadRepo.saveAll(dataUploadList);
                    dataUploadList.clear();  // Clear the list after saving
                }
            }

            // Save any remaining records in the final batch
            if (!dataUploadList.isEmpty()) {
                dataUploadRepo.saveAll(dataUploadList);
            }

        }

    }
}
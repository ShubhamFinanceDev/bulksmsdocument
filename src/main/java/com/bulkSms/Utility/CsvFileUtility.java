package com.bulkSms.Utility;

import com.bulkSms.Entity.BulkSms;
import com.bulkSms.Entity.DataUpload;
import com.bulkSms.Model.CommonResponse;
import com.bulkSms.Repository.DataUploadRepo;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        Date d1 = new Date();

        try (BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8")); CSVParser csvParser = new CSVParser(bReader, CSVFormat.DEFAULT.withDelimiter('|').withTrim())) {

            for (CSVRecord record : csvParser) {
                String loanNumber = record.get(0);
                String certificateCategory = record.get(2);
                String mobileNo=record.get(1);
                // Combine loanNumber and certificateCategory to create a unique key
                String uniqueKey = loanNumber + "|" + certificateCategory;

                if (uniqueRecords.contains(uniqueKey)) {
                    // Skip this record if it's already in the Set
                    log.info("Duplicate found for LoanNumber: " + loanNumber + ", CertificateCategory: " + certificateCategory);
                    continue;
                } else {
                    // Add the unique key to the Set
                    uniqueRecords.add(uniqueKey);
                }

                // Check for existing data in the database
//                Optional<DataUpload> existingDataUpload = dataUploadRepo.findByloanNumber(loanNumber, certificateCategory);
//
//                if (existingDataUpload.isPresent()) {
//                    DataUpload dataUpload = existingDataUpload.get();
//                    dataUpload.setMobileNumber(mobileNo);
//
//                    dataUpload.setUpload_date(java.sql.Date.valueOf(java.time.LocalDate.now()));
//
//                    dataUpload.setSmsFlag("N");
//                    dataUploadRepo.save(dataUpload);
//                } else {
                    DataUpload dataUpload = new DataUpload();
                    BulkSms bulkSms = new BulkSms();
                    dataUpload.setLoanNumber(record.get(0));
                    dataUpload.setMobileNumber(record.get(1));
                    dataUpload.setCertificateCategory(record.get(2));
                    dataUpload.setUpload_date(java.sql.Date.valueOf(java.time.LocalDate.now()));
                    dataUpload.setSmsFlag("N");
                    bulkSms.setSmsTimeStamp(null);
                    bulkSms.setDataUpload(dataUpload);
                    dataUpload.setBulkSms(bulkSms);
//                    log.info("Adding into list {}",uniqueKey);
                    dataUploadList.add(dataUpload);
//                }

                // Optional: Save records in batches
                if (dataUploadList.size() == 5000) {  // Process every 100 records

                    dataUploadRepo.saveAll(dataUploadList);
                    log.info("Data save count {}",dataUploadList.size());
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
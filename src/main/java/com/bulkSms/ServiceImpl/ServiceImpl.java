package com.bulkSms.ServiceImpl;


import com.bulkSms.Entity.BulkSms;
import com.bulkSms.Entity.DataUpload;
import com.bulkSms.Entity.Role;
import com.bulkSms.Entity.UserDetail;
import com.bulkSms.Model.CommonResponse;
import com.bulkSms.Model.RegistrationDetails;
import com.bulkSms.Repository.BulkRepository;
import com.bulkSms.Repository.DataUploadRepo;
import com.bulkSms.Repository.DocumentDetailsRepo;
import com.bulkSms.Repository.UserDetailRepo;
import com.bulkSms.Service.Service;
import com.bulkSms.Utility.CsvFileUtility;
import com.bulkSms.Utility.EncodingUtils;
import com.bulkSms.Utility.SmsUtility;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class ServiceImpl implements Service {

    @Autowired
    private CsvFileUtility csvFileUtility;
    @Autowired
    private BulkRepository bulkRepository;
    @Autowired
    private EncodingUtils encodingUtils;
    @Value("${project.save.path}")
    private String projectSavePath;
    @Autowired
    private UserDetailRepo userDetailRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private BulkRepository bulkSmsRepo;
    @Autowired
    private SmsUtility smsUtility;
    @Autowired
    private DataUploadRepo dataUploadRepo;
    @Autowired
    private DocumentDetailsRepo documentDetailsRepo;

    public ResponseEntity<CommonResponse> fetchPdf(String folderPath) {
        CommonResponse commonResponse = new CommonResponse();
        File sourceFolder = new File(folderPath);

        if (!sourceFolder.exists() || !sourceFolder.isDirectory()) {
            commonResponse.setMsg("Source folder does not exist or is not a valid directory.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(commonResponse);
        }
        File[] files = sourceFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));

        if (files == null || files.length == 0) {
            commonResponse.setMsg("No PDF files found in the specified directory.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(commonResponse);
        }

        for (File sourceFile : files) {
            if (!sourceFile.exists() || !sourceFile.isFile()) {
                commonResponse.setMsg("File " + sourceFile.getName() + " does not exist or is not a valid file.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(commonResponse);
            }

            String encodedName = encodingUtils.encode(sourceFile.getName());
            System.out.println("Encoded Name: " + encodedName + " ,Decoded Name: " + encodingUtils.decode(encodedName));

            Path sourcePath = sourceFile.toPath();
            Path targetPath = Path.of(projectSavePath, sourcePath.getFileName().toString());

            try {
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                commonResponse.setMsg("An error occurred while copying the file " + sourceFile.getName() + ": " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(commonResponse);
            }
        }
        commonResponse.setMsg("All PDF files copied successfully with encoded names.");
        return ResponseEntity.ok(commonResponse);
    }

    @Override
    public ResponseEntity<CommonResponse> save(MultipartFile file) throws Exception {
        CommonResponse commonResponse = new CommonResponse();

        if (csvFileUtility.hasCsvFormat(file)) {
            List<DataUpload> dataUploadList = csvFileUtility.csvBulksms(file.getInputStream());
            dataUploadRepo.saveAll(dataUploadList);
            commonResponse.setMsg("Csv file upload successfully");
        } else {
            commonResponse.setMsg("File is not a csv file");
        }
        return ResponseEntity.ok(commonResponse);
    }

    @Override
    public void registerNewUser(RegistrationDetails registerUserDetails) throws Exception {
        if(userDetailRepo.findByEmailId(registerUserDetails.getEmailId()).isPresent()){
            throw new Exception("EmailID already exist");
        }
        UserDetail userDetails = new UserDetail();
        userDetails.setFirstname(registerUserDetails.getFirstName());
        userDetails.setLastName(registerUserDetails.getLastName());
        userDetails.setEmailId(registerUserDetails.getEmailId());
        userDetails.setMobileNo(registerUserDetails.getMobileNo());
        userDetails.setPassword(passwordEncoder.encode(registerUserDetails.getPassword()));

        Role role = new Role();
        String roleName = registerUserDetails.getRole() != null ? registerUserDetails.getRole() : "ROLE_USER";
        role.setRole(roleName);
        role.setUserMaster(userDetails);

        userDetails.setRoleMaster(role);

        userDetailRepo.save(userDetails);

        registerUserDetails.setRole(role.getRole());
    }


    @Override
    public Page<Object> sendSmsToUser(String smsCategory, Pageable pageable) throws Exception {
        List<Object> content = new ArrayList<>();
        LocalDateTime timestamp = LocalDateTime.now();
        List<BulkSms> bulkSmsList = new ArrayList<>();

        try {
            Page<DataUpload> smsCategoryDetails = dataUploadRepo.findByCategoryAndSmsFlagNotSent(smsCategory,pageable);
            if (smsCategoryDetails != null && !smsCategoryDetails.isEmpty()) {
                for (DataUpload smsSendDetails : smsCategoryDetails) {

                    String loanDetails="/sms-service/download-pdf/"+encodingUtils.encode(smsSendDetails.getLoanNumber());
                    if(documentDetailsRepo.findDocumentByLoanNumber(loanDetails).isPresent()){
                        smsUtility.sendTextMsgToUser(smsSendDetails);

                        BulkSms bulkSms = new BulkSms();
                        bulkSms.setSmsTimeStamp(timestamp);
                        bulkSms.setDataUpload(smsSendDetails);
                        bulkSmsList.add(bulkSms);

                        smsSendDetails.setSmsFlag("Y");
                        dataUploadRepo.save(smsSendDetails);

                        Map<Object, Object> map = new HashMap<>();
                        map.put("loanNumber", smsSendDetails.getLoanNumber());
                        map.put("mobileNumber", smsSendDetails.getMobileNumber());
                        map.put("timestamp", timestamp);
                        map.put("smsFlag", "Y");
                        content.add(map);

                    }
                    }
                bulkSmsRepo.saveAll(bulkSmsList);
                }

            return new PageImpl<>(content, pageable, smsCategoryDetails.getTotalElements());

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public Page<Object> listOfSendSmsToUser(String smsCategory, Pageable pageable) throws Exception {
        List<Object> userDetails = new ArrayList<>();
        LocalDateTime timeStamp = LocalDateTime.now();

        try {
            Page<DataUpload> userDetailsList;

            if (smsCategory == null || smsCategory.isEmpty()) {

                userDetailsList = dataUploadRepo.findByType(pageable);
            } else {

                userDetailsList = dataUploadRepo.findBySmsCategory(smsCategory, pageable);
            }

            if (userDetailsList.hasContent()) {
                for (DataUpload userDetail : userDetailsList) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("loanNumber", userDetail.getLoanNumber());
                    map.put("mobileNumber", userDetail.getMobileNumber());
                    map.put("timestamp", timeStamp);
                    map.put("smsFlag", userDetail.getSmsFlag());
                    userDetails.add(map);
                }
            }

            return new PageImpl<>(userDetails, pageable, userDetailsList.getTotalElements());

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

}
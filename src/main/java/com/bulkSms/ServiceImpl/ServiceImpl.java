package com.bulkSms.ServiceImpl;


import com.bulkSms.Entity.*;
import com.bulkSms.Model.*;
import com.bulkSms.Repository.*;
import com.bulkSms.Service.Service;
import com.bulkSms.Utility.CsvFileUtility;
import com.bulkSms.Utility.EncodingUtils;
import com.bulkSms.Utility.SmsUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

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
    private JobAuditTrailRepo jobAuditTrailRepo;
    @Autowired
    private DocumentDetailsRepo documentDetailsRepo;
    @Autowired
    private BulkRepository bulkSmsRepo;
    @Autowired
    private SmsUtility smsUtility;
    @Autowired
    private DataUploadRepo dataUploadRepo;

    private final ResourceLoader resourceLoader;

    public ServiceImpl(ResourceLoader resourceLoader, @Value("${project.save.path}") String projectSavePath) {
        this.resourceLoader = resourceLoader;
        this.projectSavePath = projectSavePath;
    }

    ;

    public ResponseEntity<?> fetchPdf(String folderPath) {
        CommonResponse commonResponse = new CommonResponse();
        ResponseOfFetchPdf response = new ResponseOfFetchPdf();
        JobAuditTrail jobAuditTrail = new JobAuditTrail();
        List<DocumentDetails> documentReaderList = new ArrayList<>();
        File sourceFolder = new File(folderPath);

        jobAuditTrail.setJobName("Invoke_file");
        jobAuditTrail.setStatus("in_progress");
        jobAuditTrail.setStartDate(Timestamp.valueOf(LocalDateTime.now()));
        jobAuditTrailRepo.save(jobAuditTrail);

        if (!sourceFolder.exists() || !sourceFolder.isDirectory()) {
            commonResponse.setMsg("Source folder does not exist or is not a valid directory.");
            jobAuditTrailRepo.updateIfException(commonResponse.getMsg(), "failed", Timestamp.valueOf(LocalDateTime.now()), jobAuditTrail.getJobId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(commonResponse);
        }
        File[] files = sourceFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));

        if (files == null || files.length == 0) {
            commonResponse.setMsg("No PDF files found in the specified directory.");
            jobAuditTrailRepo.updateIfException(commonResponse.getMsg(), "failed", Timestamp.valueOf(LocalDateTime.now()), jobAuditTrail.getJobId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(commonResponse);
        }
        String baseDownloadUrl = "/sms-service/download-pdf/";

        for (File sourceFile : files) {
            if (!sourceFile.exists() || !sourceFile.isFile()) {
                commonResponse.setMsg("File " + sourceFile.getName() + " does not exist or is not a valid file.");
                jobAuditTrailRepo.updateIfException(commonResponse.getMsg(), "failed", Timestamp.valueOf(LocalDateTime.now()), jobAuditTrail.getJobId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(commonResponse);
            }

            String encodedName = encodingUtils.encode(sourceFile.getName().replace(".pdf", ""));
            System.out.println("Encoded Name: " + encodedName + " ,Decoded Name: " + encodingUtils.decode(encodedName));

            Path sourcePath = sourceFile.toPath();
            Path targetPath = Path.of(projectSavePath, sourcePath.getFileName().toString());

            try {
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                DocumentDetails documentReader = new DocumentDetails();
                documentReader.setJobId(jobAuditTrail.getJobId());
                documentReader.setFileName(sourceFile.getName().replace(".pdf", ""));
                documentReader.setUploadedTime(Timestamp.valueOf(LocalDateTime.now()).toLocalDateTime());
                documentReader.setDownloadUrl(baseDownloadUrl + encodedName);
                documentReader.setDownloadCount(0L);

                documentReaderList.add(documentReader);

            } catch (IOException e) {
                commonResponse.setMsg("An error occurred while copying the file " + sourceFile.getName() + ": " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(commonResponse);
            }
        }

        documentDetailsRepo.saveAll(documentReaderList);
        jobAuditTrailRepo.updateEndStatus("Number of files saved into bucket: " + files.length, "complete", Timestamp.valueOf(LocalDateTime.now()), jobAuditTrail.getJobId());
        setResponse(response);
        commonResponse.setMsg("All PDF files copied successfully with encoded names.");
        response.setCommonResponse(commonResponse);
        return ResponseEntity.ok(response);
    }


    private void setResponse(ResponseOfFetchPdf response) {
        List<DocumentDetails> documentReaderList = documentDetailsRepo.findAll();
        List<ListResponse> readerList = new ArrayList<>();
        for (DocumentDetails reader : documentReaderList) {
            ListResponse listResponse = new ListResponse();
            listResponse.setFileName(reader.getFileName());
            listResponse.setDownloadCount(reader.getDownloadCount());
            listResponse.setUploadTime(LocalDateTime.from(reader.getUploadedTime().toLocalTime()));
            listResponse.setDownloadUrl(reader.getDownloadUrl());
            readerList.add(listResponse);
        }
        response.setListOfPdfNames(readerList);
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
        if (userDetailRepo.findByEmailId(registerUserDetails.getEmailId()).isPresent()) {
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
    public ResponseEntity<?> fetchPdfFileForDownload(String loanNo) throws Exception {
        CommonResponse commonResponse = new CommonResponse();
        System.out.println(loanNo);
        DocumentDetails documentReader = documentDetailsRepo.findByLoanNo(loanNo);

        if (documentReader == null) {
            commonResponse.setMsg("File not found or invalid loanNo");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(commonResponse);
        }
        Path filePath = Paths.get(projectSavePath, loanNo + ".pdf");
        Resource resource = resourceLoader.getResource("file:" + filePath);
        ResponseEntity<Resource> response = ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + loanNo + ".pdf\"").body(resource);

        if (response.getStatusCode() == HttpStatus.OK) {
            documentDetailsRepo.updateDownloadCount(String.valueOf(filePath.getFileName()).replace(".pdf", ""), Timestamp.valueOf(LocalDateTime.now()));
        }
        return response;
    }

    public List<Object> sendSmsToUser(String smsCategory) throws Exception {
        List<Object> list = new ArrayList<>();
        LocalDateTime timestamp = LocalDateTime.now();
        List<BulkSms> bulkSmsList = new ArrayList<>();

        try {
            List<DataUpload> smsCategoryDetails = dataUploadRepo.findByCategoryAndSmsFlagNotSent(smsCategory);
            if (smsCategoryDetails != null && !smsCategoryDetails.isEmpty()) {
                for (DataUpload smsSendDetails : smsCategoryDetails) {

                    String loanDetails = "/sms-service/download-pdf/" + encodingUtils.encode(smsSendDetails.getLoanNumber());
                    if (documentDetailsRepo.findDocumentByLoanNumber(loanDetails).isPresent()) {
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
                        list.add(map);

                    }
                }
                bulkSmsRepo.saveAll(bulkSmsList);
            }

            return list;

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public List<Object> ListOfSendSmsToUser(String smsCategory) throws Exception {
        List<Object> userDetails = new ArrayList<>();
        LocalDateTime timeStamp = LocalDateTime.now();

        try {

            if (smsCategory == null || smsCategory.isEmpty()) {
                List<DataUpload> userDetails1 = dataUploadRepo.findByType();
                if (userDetails1 != null && !userDetails1.isEmpty()) {
                    for (DataUpload userDetailsList : userDetails1) {

                        Map<String, Object> map = new HashMap<>();
                        map.put("loanNumber", userDetailsList.getLoanNumber());
                        map.put("mobileNumber", userDetailsList.getMobileNumber());
                        map.put("timestamp", timeStamp);
                        map.put("smsFlag", userDetailsList.getSmsFlag());
                        userDetails.add(map);
                    }
                }
            } else {
                List<DataUpload> userDetailsList = dataUploadRepo.findBySmsCategory(smsCategory);
                if (userDetailsList != null && !userDetailsList.isEmpty()) {
                    for (DataUpload userDetail : userDetailsList) {

                        Map<String, Object> map = new HashMap<>();
                        map.put("loanNumber", userDetail.getLoanNumber());
                        map.put("mobileNumber", userDetail.getMobileNumber());
                        map.put("timestamp", timeStamp);
                        map.put("smsFlag", userDetail.getSmsFlag());
                        userDetails.add(map);
                    }
                }
            }
            return userDetails;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    public ResponseEntity<?> getDashboardData() throws Exception {

        CommonResponse commonResponse = new CommonResponse();
        DashboardResponse dashboardResponse = new DashboardResponse();
        List<DashboardDataList> lists = new ArrayList<>();

        Long downloadCount = documentDetailsRepo.getDownloadCount();
        Long smsCount = dataUploadRepo.getSmsCount();
        List<DataUpload> dataUpload = dataUploadRepo.findByType();

        if (dataUpload.isEmpty()) {
            commonResponse.setMsg("Data not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(commonResponse);
        }

        for (DataUpload data : dataUpload) {
            DashboardDataList dashboardData = new DashboardDataList();
            dashboardData.setCategory(data.getCertificateCategory());
            dashboardData.setPhoneNo(data.getMobileNumber());
            dashboardData.setSmsTimeStamp(data.getBulkSms().getSmsTimeStamp());
            dashboardData.setLoanNo(data.getLoanNumber());
            Optional<DocumentDetails> documentDetails = documentDetailsRepo.findDataByLoanNo(data.getLoanNumber());
            if (documentDetails.isPresent() && (documentDetails.get().getDownloadCount() > 0)) {
                dashboardData.setDownloadCount(documentDetails.get().getDownloadCount());
                dashboardData.setLastDownload(documentDetails.get().getLastDownload());
                lists.add(dashboardData);

            } else {
                System.out.println("No DocumentDetails found for loan number: " + data.getLoanNumber());
            }

        }

        dashboardResponse.setDataLists(lists);
        dashboardResponse.setSmsCount(smsCount);
        dashboardResponse.setDownloadCount(downloadCount);
        commonResponse.setMsg("Data found successfully.");

        return ResponseEntity.ok(dashboardResponse);
    }

}
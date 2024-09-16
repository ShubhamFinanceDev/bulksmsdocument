package com.bulkSms.ServiceImpl;


import com.bulkSms.Entity.*;
import com.bulkSms.Model.CommonResponse;
import com.bulkSms.Model.ListResponse;
import com.bulkSms.Model.RegistrationDetails;
import com.bulkSms.Model.ResponseOfFetchPdf;
import com.bulkSms.Repository.BulkRepository;
import com.bulkSms.Repository.DocumentReaderRepo;
import com.bulkSms.Repository.JobAuditTrailRepo;
import com.bulkSms.Repository.UserDetailRepo;
import com.bulkSms.Service.Service;
import com.bulkSms.Utility.CsvFileUtility;
import com.bulkSms.Utility.EncodingUtils;
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
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
public class ServiceImpl implements Service {

    @Autowired
    private CsvFileUtility csvFileUtility;
    @Autowired
    private BulkRepository bulkRepository;
    @Autowired
    private EncodingUtils encodingUtils;
    @Autowired
    private UserDetailRepo userDetailRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JobAuditTrailRepo jobAuditTrailRepo;
    @Autowired
    private DocumentReaderRepo documentReaderRepo;

    @Value("${project.save.path}")
    private final String projectSavePath;
    private final ResourceLoader resourceLoader;

    public ServiceImpl(ResourceLoader resourceLoader, @Value("${project.save.path}") String projectSavePath) {
        this.resourceLoader = resourceLoader;
        this.projectSavePath = projectSavePath;
    };

    public ResponseEntity<?> fetchPdf(String folderPath) {
        CommonResponse commonResponse = new CommonResponse();
        ResponseOfFetchPdf response = new ResponseOfFetchPdf();
        JobAuditTrail jobAuditTrail = new JobAuditTrail();
        List<DocumentReader> documentReaderList = new ArrayList<>();
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
        String baseDownloadUrl = "http://localhost:8080/sms-service/download-pdf?loanNo=";

        for (File sourceFile : files) {
            if (!sourceFile.exists() || !sourceFile.isFile()) {
                commonResponse.setMsg("File " + sourceFile.getName() + " does not exist or is not a valid file.");
                jobAuditTrailRepo.updateIfException(commonResponse.getMsg(), "failed", Timestamp.valueOf(LocalDateTime.now()), jobAuditTrail.getJobId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(commonResponse);
            }

            String encodedName = encodingUtils.encode(sourceFile.getName());
            System.out.println("Encoded Name: " + encodedName + " ,Decoded Name: " + encodingUtils.decode(encodedName));

            Path sourcePath = sourceFile.toPath();
            Path targetPath = Path.of(projectSavePath, sourcePath.getFileName().toString());

            try {
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                DocumentReader documentReader = new DocumentReader();
                documentReader.setJobId(jobAuditTrail.getJobId());
                documentReader.setFileName(sourceFile.getName());
                documentReader.setUploadedTime(Timestamp.valueOf(LocalDateTime.now()));
                documentReader.setDownloadUrl(baseDownloadUrl + encodedName);
                documentReader.setDownloadCount(0L);

                documentReaderList.add(documentReader);

            } catch (IOException e) {
                commonResponse.setMsg("An error occurred while copying the file " + sourceFile.getName() + ": " + e.getMessage());
                jobAuditTrailRepo.updateIfException(commonResponse.getMsg(), "failed", Timestamp.valueOf(LocalDateTime.now()), jobAuditTrail.getJobId());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(commonResponse);
            }
        }

        documentReaderRepo.saveAll(documentReaderList);
        jobAuditTrailRepo.updateEndStatus("Number of files saved into bucket: " + files.length, "complete", Timestamp.valueOf(LocalDateTime.now()), jobAuditTrail.getJobId());
        setResponse(response);
        commonResponse.setMsg("All PDF files copied successfully with encoded names.");
        response.setCommonResponse(commonResponse);
        return ResponseEntity.ok(response);
    }


    private void setResponse(ResponseOfFetchPdf response) {
        List<DocumentReader> documentReaderList = documentReaderRepo.findAll();
        List<ListResponse> readerList = new ArrayList<>();
        for (DocumentReader reader : documentReaderList) {
            ListResponse listResponse = new ListResponse();
            listResponse.setFileName(reader.getFileName());
            listResponse.setDownloadCount(reader.getDownloadCount());
            listResponse.setDownloadTime(reader.getUploadedTime().toLocalDateTime());
            listResponse.setDownloadUrl(reader.getDownloadUrl());
            readerList.add(listResponse);
        }
        response.setListOfPdfNames(readerList);
    }

    @Override
    public ResponseEntity<CommonResponse> save(MultipartFile file) throws Exception {
        CommonResponse commonResponse = new CommonResponse();

        if (csvFileUtility.hasCsvFormat(file)) {
            List<BulkSms> bulkSmsList = csvFileUtility.csvBulksms(file.getInputStream());
            bulkRepository.saveAll(bulkSmsList);
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
    public ResponseEntity<?> fetchPdfFile(String loanNo) throws Exception {
        CommonResponse commonResponse = new CommonResponse();

        Path filePath = Paths.get(projectSavePath, loanNo /*+ ".pdf"*/);
        if (!Files.exists(filePath)) {
            commonResponse.setMsg("File not found or invalid loanNo");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(commonResponse);
        }
        Resource resource = resourceLoader.getResource("file:" + filePath);
        ResponseEntity<Resource> response = ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + loanNo + ".pdf\"").body(resource);

        if (response.getStatusCode() == HttpStatus.OK) {
            documentReaderRepo.updateDownloadCount(String.valueOf(filePath.getFileName()));
        }
        return response;
    }
}
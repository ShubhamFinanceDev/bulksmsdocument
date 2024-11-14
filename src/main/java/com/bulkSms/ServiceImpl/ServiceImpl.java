package com.bulkSms.ServiceImpl;


import com.bulkSms.Entity.*;
import com.bulkSms.Entity.DataUpload;
import com.bulkSms.Entity.Role;
import com.bulkSms.Entity.UserDetail;
import com.bulkSms.Model.*;
import com.bulkSms.Repository.*;
import com.bulkSms.Service.Service;
import com.bulkSms.Utility.CsvFileUtility;
import com.bulkSms.Utility.EncodingUtils;
import com.bulkSms.Utility.SmsUtility;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
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
    private DocumentDetailsRepo documentDetailsRepo;
    @Autowired
    private BulkRepository bulkSmsRepo;
    @Autowired
    private SmsUtility smsUtility;
    @Autowired
    private DataUploadRepo dataUploadRepo;

    @Value("${project.save.path.adhoc}")
    private String projectSavePathAdhoc;

    @Value("${project.save.path.soa}")
    private String projectSavePathSoa;
    @Value("${project.save.path.intrest.certificate}")
    private String projectSavePathInterestCertificate;

    @Value("${project.save.path.payment.reminder}")
    private String projectSavePathPaymentReminder;
    @Value("${project.save.path.soa.quarterly}")
    private String projectSavePathSoaQuarterly;

    private ResourceLoader resourceLoader;

    private String destinationStorage(String category) {
        return category.equals("ADHOC") ? projectSavePathAdhoc :
                category.equals("SOA") || category.equals("SOA_QUARTERLY") ? projectSavePathSoa :
                        category.equals("INTEREST_CERTIFICATE") ? projectSavePathInterestCertificate : null;

    }

    @Transactional
    public ResponseEntity<?> fetchPdf(String folderPath, String category) throws IOException {
        CommonResponse commonResponse = new CommonResponse();
        ResponseOfFetchPdf response = new ResponseOfFetchPdf();
        JobAuditTrail jobAuditTrail = new JobAuditTrail();
        jobAuditTrail.setJobName("Upload-file");
        jobAuditTrail.setStatus("in_progress");
        jobAuditTrail.setStartDate(Timestamp.valueOf(LocalDateTime.now()));
        jobAuditTrailRepo.save(jobAuditTrail);
        jobAuditTrailRepo.save(jobAuditTrail);

        List<DocumentDetails> documentReaderList = new ArrayList<>();
        String copyPath = destinationStorage(category);
        long count = 0;

        try {
            if (category.equals("ADHOC")) {
                try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Path.of(folderPath))) {
                    for (Path subDir : directoryStream) {
//                        if (count == 0) {count++; continue;}  // Skip the first iteration
                        File[] pdfFiles = subDir.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
                        if (pdfFiles.length > 0) {
                            PDFMergerUtility pdfMerger = new PDFMergerUtility();
                            for (File pdfFile : pdfFiles) pdfMerger.addSource(pdfFile);

                            Path mergedPDFPath = Path.of(copyPath, subDir.getFileName().toString() + ".pdf");
                            pdfMerger.setDestinationFileName(mergedPDFPath.toString());
                            pdfMerger.mergeDocuments(null);
                            documentReaderList.add(createDocumentDetails(jobAuditTrail, subDir.getFileName().toString(), category, 0L));
                            count++;
                            documentDetailsRepo.save(documentReaderList.get(documentReaderList.size() - 1));
                        }
                       else {
                            commonResponse.setMsg("Pdf not found.");
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(commonResponse);
                        }

                    }
                }
            } else {
                File[] pdfFiles = Path.of(folderPath).toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
                if (pdfFiles.length > 0) {
                    for (File pdfFile : pdfFiles) {
                        Long fileSequence = documentDetailsRepo.incrementSequence("soa_sequence");
                        String loanNo = extractFilename(pdfFile.getName());
                        String filename = loanNo + "@" + fileSequence + ".pdf";
                        Path destinationPath = Path.of(copyPath, filename); // Correctly create the destination path
                        Files.copy(pdfFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING); // Use the correct method and path
                        documentReaderList.add(createDocumentDetails(jobAuditTrail, loanNo, category, fileSequence));
                        count++;
                        documentDetailsRepo.save(documentReaderList.get(documentReaderList.size() - 1));
                    }
                } else {
                    commonResponse.setMsg("Pdf not found.");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(commonResponse);
                }

            }
            jobAuditTrailRepo.updateEndStatus("Number of files saved: " + count, "complete", Timestamp.valueOf(LocalDateTime.now()), jobAuditTrail.getJobId());
            setJobResponse(response, jobAuditTrail.getJobId(), commonResponse, count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            handleException(e, commonResponse, jobAuditTrail);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(commonResponse);
    }

    private DocumentDetails createDocumentDetails(JobAuditTrail jobAuditTrail, String fileName, String category, Long fileSequence) {
        DocumentDetails documentReader = new DocumentDetails();
        documentReader.setJobId(jobAuditTrail.getJobId());
        documentReader.setFileName(fileName);
        documentReader.setUploadedTime(Timestamp.valueOf(LocalDateTime.now()));
        documentReader.setCategory(category);
        documentReader.setDownloadCount(0L);
        documentReader.setFile_sequence(fileSequence);
        return documentReader;
    }

    private String extractFilename(String fileName) {
        int firstUnderscore = fileName.indexOf('_');
        int secondUnderscore = fileName.indexOf('_', firstUnderscore + 1);
        int thirdUnderscore = fileName.indexOf('_', secondUnderscore + 1);
        return fileName.substring(secondUnderscore + 1, thirdUnderscore);
    }

    private void handleException(Exception e, CommonResponse commonResponse, JobAuditTrail jobAuditTrail) {
        commonResponse.setMsg("Error copying file: " + e.getMessage());
        jobAuditTrailRepo.updateIfException(commonResponse.getMsg(), "failed", Timestamp.valueOf(LocalDateTime.now()), jobAuditTrail.getJobId());
    }

    private void setJobResponse(ResponseOfFetchPdf response, Long jobId, CommonResponse commonResponse, long count) {
        commonResponse.setMsg("All PDF files copied successfully.");
        response.setCommonResponse(commonResponse);
        response.setDownloadCount(count);
        Pageable pageable = PageRequest.of(0, 1000);

        List<DocumentDetails> latestCopedFile = documentDetailsRepo.finByJobId(jobId, pageable);

        List<ListResponse> readerList = new ArrayList<>();
        for (DocumentDetails reader : latestCopedFile) {
            ListResponse listResponse = new ListResponse();
            listResponse.setFileName(reader.getFileName());
            listResponse.setUploadTime(reader.getUploadedTime().toLocalDateTime());
            listResponse.setCategory(reader.getCategory());
            readerList.add(listResponse);
        }
        response.setListOfPdfNames(readerList);
    }


    @Override
    public ResponseEntity<CommonResponse> csvFileUploadSave(MultipartFile file) throws Exception {
        CommonResponse commonResponse = new CommonResponse();
        if (csvFileUtility.hasCsvFormat(file)) {
            csvFileUtility.readCsvFile(file.getInputStream());
            commonResponse.setMsg("Files have been uploaded successfully.");
        } else {
            commonResponse.setMsg("File is not a csv file or empty");
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

//    @Override
//    public ResponseEntity<?> fetchPdfFileForDownload(String loanNo,String category) throws Exception {
//        CommonResponse commonResponse = new CommonResponse();
//        System.out.println(loanNo);
//        DocumentDetails documentReader = documentDetailsRepo.findByLoanNo(loanNo);
//
//        if (documentReader == null) {
//            commonResponse.setMsg("File not found or invalid loanNo");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(commonResponse);
//        }
//        if(category.contains("ADHOC"))
//        Path filePath = Paths.get(projectSavePath, loanNo + ".pdf");
//        Resource resource = resourceLoader.getResource("file:" + filePath);
//
//        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + loanNo + ".pdf\"").body(resource);
//    }

    @Override
    public ResponseEntity<?> sendSmsToUser(String smsCategory) throws Exception {
        List<Object> content = new ArrayList<>();
        CommonResponse commonResponse = new CommonResponse();

        try {
            int requestBatchSize = 5000;
            int batchCount = 0;

            while (true) {
                Pageable pageable = PageRequest.of(batchCount, requestBatchSize);
                Page<GetDataForSendSms> smsCategoryDetails = dataUploadRepo.findByCategoryAndSmsFlagNotSent(smsCategory, pageable);
                if (smsCategoryDetails.hasContent()) {
                    List<GetDataForSendSms> dataUploadList = smsCategoryDetails.getContent();
                    log.info("List size fetched {} for batchCount {}", dataUploadList.size(), batchCount);
                    String category = smsCategory;
                    executeSmsServiceThread(dataUploadList, content, category); //start send sms thread
                    batchCount++;

                } else {
                    break;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
        if (content.isEmpty()) {
            commonResponse.setMsg("Data not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(commonResponse);
        } else {
            commonResponse.setMsg("Success");
            return ResponseEntity.status(HttpStatus.OK).body(new SmsResponse(content.size(), commonResponse.getMsg(), content));

        }
    }


    private void executeSmsServiceThread(List<GetDataForSendSms> dataUploadList, List<Object> content, String category) throws Exception {
        LocalDateTime timestamp = LocalDateTime.now();
        log.info("Snd-sms thread service started for list size {}", dataUploadList.size());
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        log.info("Current available processors {}", availableProcessors);

        int listSize = dataUploadList.size();
        int batchSize = 500; // Adjust as needed based on memory or processing needs
        int numBatches = (int) Math.ceil((double) listSize / batchSize);  // Total number of batches
        int numThreads = Math.min(numBatches, availableProcessors * 2);

        log.info("No of threads set in poll size {}", numThreads);
        // Create a fixed thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        int start = 0;

        for (int i = 0; i < numThreads; i++) {

            int end = Math.min(start + batchSize, dataUploadList.size());
            // Sublist for each thread to process
            List<GetDataForSendSms> sublist = dataUploadList.subList(start, end);
            log.info("Thread {} execution initiated and processing list index from {} to {}", numThreads, start, end);
            // Submit a task to process this sublist
            executorService.submit(() -> {
                for (GetDataForSendSms element : sublist) {
                    try {

                        smsUtility.sendTextMsgToUser(element);
                        bulkSmsRepo.updateBulkSmsTimestampByDataUploadId(element.getId());
                        Map<Object, Object> map = new HashMap<>();
                        map.put("loanNumber", element.getLoanNumber());
                        map.put("mobileNumber", element.getMobileNumber());
                        map.put("timestamp", timestamp);
                        map.put("smsFlag", "success");
                        map.put("category", category);
                        content.add(map);
//                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }

                }
                log.info("Thread {} completed successfully sms sent successfully {}  ", numThreads, sublist.size());

            });


            start = end;

        }
        // Shut down the executor and wait for tasks to complete
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);
        System.out.println("All tasks completed.");


    }

    @Override
    public ResponseEntity<?> listOfSendSmsToUser(String smsCategory, int pageNo) throws Exception {
        List<Object> userDetails = new ArrayList<>();
        LocalDateTime timeStamp = LocalDateTime.now();
        long detailOfCount = 0;
        int pageSize = 100;
        CommonResponse commonResponse = new CommonResponse();

        try {
            Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
            List<DataUpload> userDetailsList;
            if (smsCategory == null || smsCategory.isEmpty()) {

                userDetailsList = dataUploadRepo.findByTypeOfSendSms(pageable);
                detailOfCount = dataUploadRepo.findCount();
            } else {

                userDetailsList = dataUploadRepo.findBySmsCategoryOfSendSms(smsCategory, pageable);
                detailOfCount = dataUploadRepo.findCountWithSmsCategory(smsCategory);
            }

            if (!userDetailsList.isEmpty() && detailOfCount > 0) {
                for (DataUpload userDetail : userDetailsList) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("loanNumber", userDetail.getLoanNumber());
                    map.put("mobileNumber", userDetail.getMobileNumber());
                    map.put("timestamp", timeStamp);
                    map.put("smsFlag", "success");
                    map.put("category", userDetail.getCertificateCategory());
                    userDetails.add(map);

                }
                return ResponseEntity.status(HttpStatus.OK).body(new SmsResponse(detailOfCount, pageNo <= (detailOfCount / pageSize), "success", userDetails));

            } else {
                commonResponse.setMsg("Data not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(commonResponse);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    public ResponseEntity<?> getDashboardData(int pageNo) throws Exception {

        CommonResponse commonResponse = new CommonResponse();
        DashboardResponse dashboardResponse = new DashboardResponse();
        List<DashboardDataList> lists = new ArrayList<>();
        Map<String, Long> smsCountByCategory = new HashMap<>();
        Map<String, Long> downloadCountByCategory = new HashMap<>();
        int pageSize = 100;

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        List<Object[]> smsCountByCategoryData = dataUploadRepo.countSmsByCategory();
        List<Object[]> downloadCountByCategoryData = documentDetailsRepo.countDownloadByCategory();
        List<DashboardDataList> dataUpload = dataUploadRepo.findByType(pageable);
        double totalCount = dataUploadRepo.listTotalDownloadCount();
        setDownloadAndSmsCount(smsCountByCategoryData, smsCountByCategory);
        setDownloadAndSmsCount(downloadCountByCategoryData, downloadCountByCategory);
        dashboardResponse.setDataLists(dataUpload);
        dashboardResponse.setTotalCount((long) totalCount);
        dashboardResponse.setNextPage(pageNo < totalCount / pageSize);
        dashboardResponse.setSmsCountByCategory(smsCountByCategory);
        dashboardResponse.setDownloadCountByCategory(downloadCountByCategory);
        commonResponse.setMsg("Data found successfully.");
        return ResponseEntity.ok(dashboardResponse);
    }

    private void setDownloadAndSmsCount(List<Object[]> countData, Map<String, Long> countMap) {
        for (Object[] row : countData) {
            countMap.put((String) row[1], (Long) row[0]);
        }
    }

    public ResponseEntity<byte[]> fetchPdfFileForDownloadBySmsLink(String filename, String category) throws Exception {

        System.out.println("decode loan no" + filename);
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        if (category.contains("ADHOC")) {
            return generatePdfDocument(filename, projectSavePathAdhoc, category);
        } else if (category.contains("SOA")) {
            return generatePdfDocument(filename, projectSavePathSoa, category);

        } else if (category.contains("INTEREST_CERTIFICATE")) {
            return generatePdfDocument(filename, projectSavePathInterestCertificate, category);

        }
        return null;
    }

    private ResponseEntity<byte[]> generatePdfDocument(String fileName, String projectSavePathPaymentReminder, String category) throws IOException {
        String loanNo;
        if (fileName.contains("@")) {
            int underscoreIndex = fileName.indexOf("@");
            loanNo = fileName.substring(0, underscoreIndex);
        } else {
            loanNo = fileName;
        }
        fileName=fileName+".pdf";


        Path filePath = Paths.get(projectSavePathPaymentReminder);
        File pdfFile = new File(filePath + fileName);
        System.out.println("filepath" + filePath);
        if (pdfFile.exists()) {
            System.out.println("File not found");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        }
        byte[] pdfBytes;
        InputStream inputStream = new FileInputStream(filePath + "/" + fileName);
        pdfBytes = inputStream.readAllBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", fileName);
        documentDetailsRepo.updateDownloadCountBySmsLink(loanNo, Timestamp.valueOf(LocalDateTime.now()), category);
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @Override
    public ResponseEntity<?> listOfUnsendSms(String smsCategory, int pageNo) throws Exception {
        List<Object> detailsOfUser = new ArrayList<>();
        LocalDateTime timeStamp = LocalDateTime.now();
        long detailOfCount = 0;
        int pageSize = 100;
        CommonResponse commonResponse = new CommonResponse();

        try {
            Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
            List<DataUpload> unsendSmsDetails;
            if (smsCategory == null || smsCategory.isEmpty()) {

                unsendSmsDetails = dataUploadRepo.findByTypeForUnsendSms(pageable);
                detailOfCount = dataUploadRepo.findUnsendSmsCountByType();
            } else {

//                unsendSmsDetails = dataUploadRepo.findBySmsCategoryForUnsendSms(smsCategory, pageable);
                unsendSmsDetails = dataUploadRepo.findBySmsCategoryForUnsendSms(smsCategory);
                detailOfCount = dataUploadRepo.findUnsendSmsCountByCategory(smsCategory);
            }

            if (!unsendSmsDetails.isEmpty() && detailOfCount > 0) {
                for (DataUpload userDetail : unsendSmsDetails) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("loanNumber", userDetail.getLoanNumber());
                    map.put("mobileNumber", userDetail.getMobileNumber());
                    map.put("timestamp", timeStamp);
                    map.put("smsFlag", "un-send");
                    map.put("category", userDetail.getCertificateCategory());
                    detailsOfUser.add(map);
                }
                return ResponseEntity.status(HttpStatus.OK).body(new SmsResponse(detailOfCount, pageNo <= (detailOfCount / pageSize), "success", detailsOfUser));

            } else {
                commonResponse.setMsg("Data not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(commonResponse);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }


}
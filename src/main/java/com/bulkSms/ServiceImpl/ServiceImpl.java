package com.bulkSms.ServiceImpl;


import com.bulkSms.Entity.*;
import com.bulkSms.Entity.BulkSms;
import com.bulkSms.Entity.DataUpload;
import com.bulkSms.Entity.Role;
import com.bulkSms.Entity.UserDetail;
import com.bulkSms.Model.*;
import com.bulkSms.Repository.*;
import com.bulkSms.Service.Service;
import com.bulkSms.Utility.CsvFileUtility;
import com.bulkSms.Utility.EncodingUtils;
import com.bulkSms.Utility.SmsUtility;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
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

    private ResourceLoader resourceLoader;

    private String destinationStorage(String category) {
        return category.contains("ADHOC") ? projectSavePathAdhoc :
                category.contains("SOA") ? projectSavePathSoa :
                        category.contains("INTEREST_CERTIFICATE") ? projectSavePathInterestCertificate :
                                category.contains("Reminder_Payment") ? projectSavePathPaymentReminder : null;
    }

    @Transactional
    public ResponseEntity<?> fetchPdf(String folderPath, String category, int pageNo) throws IOException {

        CommonResponse commonResponse = new CommonResponse();
        DocumentDetails documentReader = new DocumentDetails();
        ResponseOfFetchPdf response = new ResponseOfFetchPdf();
        JobAuditTrail jobAuditTrail = new JobAuditTrail();
        List<DocumentDetails> documentReaderList = new ArrayList<>();
        String copyPath = destinationStorage(category);
        long count = 0L;
        jobAuditTrail.setJobName("Upload-file");
        jobAuditTrail.setStatus("in_progress");
        jobAuditTrail.setStartDate(Timestamp.valueOf(LocalDateTime.now()));
        jobAuditTrailRepo.save(jobAuditTrail);

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Path.of(folderPath))) {
            for (Path subDir : directoryStream) {
                File[] pdfFiles = subDir.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));

                if (pdfFiles.length > 0) {
                    PDFMergerUtility pdfMerger = new PDFMergerUtility();
                    for (File pdfFile : pdfFiles) {
                        pdfMerger.addSource(pdfFile);
                    }
                    Path mergedPDFPath = Path.of(copyPath, subDir.getFileName().toString() + ".pdf");
                    pdfMerger.setDestinationFileName(mergedPDFPath.toString());
                    pdfMerger.mergeDocuments(null); // Merges the PDFs
                    System.out.println("Merged and copied PDF to: " + mergedPDFPath);

                    documentReader.setJobId(jobAuditTrail.getJobId());
                    documentReader.setFileName(String.valueOf(subDir.getFileName()));
                    documentReader.setUploadedTime(Timestamp.valueOf(LocalDateTime.now()));
                    documentReader.setCategory(category);
                    documentReader.setDownloadCount(0L);
                    documentReaderList.add(documentReader);
                    count++;
                    documentDetailsRepo.save(documentReader);

                    jobAuditTrailRepo.updateEndStatus("Number of files saved into bucket: " + count, "complete", Timestamp.valueOf(LocalDateTime.now()), jobAuditTrail.getJobId());
                    commonResponse.setMsg("All PDF files copied successfully.");

                }
            }
            setJobResponse(response, jobAuditTrail.getJobId());
            response.setCommonResponse(commonResponse);
            response.setDownloadCount(count);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            commonResponse.setMsg("An error occurred while copying the file " + e.getMessage());
            jobAuditTrailRepo.updateIfException(commonResponse.getMsg(), "failed", Timestamp.valueOf(LocalDateTime.now()), jobAuditTrail.getJobId());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(commonResponse);

        }

    }


    private void setJobResponse(ResponseOfFetchPdf responseOfFetchPdf, Long jobId) {
        List<DocumentDetails> latestCopedFile = documentDetailsRepo.finByJobId(jobId);

        List<ListResponse> readerList = new ArrayList<>();
        for (DocumentDetails reader : latestCopedFile) {
            ListResponse listResponse = new ListResponse();
            listResponse.setFileName(reader.getFileName());
            listResponse.setUploadTime(reader.getUploadedTime().toLocalDateTime());
            listResponse.setCategory(reader.getCategory());
            readerList.add(listResponse);
        }
        responseOfFetchPdf.setListOfPdfNames(readerList);
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
        LocalDateTime timestamp = LocalDateTime.now();

        try {
            CommonResponse commonResponse = new CommonResponse();
            List<DataUpload> smsCategoryDetails = dataUploadRepo.findByCategoryAndSmsFlagNotSent(smsCategory);
            if (smsCategoryDetails != null && !smsCategoryDetails.isEmpty()) {
                for (DataUpload smsSendDetails : smsCategoryDetails) {

                    if (documentDetailsRepo.findByLoanNoAndCategory(smsSendDetails.getLoanNumber(), smsCategory) != null) {

                        smsUtility.sendTextMsgToUser(smsSendDetails);
                        bulkSmsRepo.updateBulkSmsTimestampByDataUploadId(smsSendDetails.getId());

                        Map<Object, Object> map = new HashMap<>();
                        map.put("loanNumber", smsSendDetails.getLoanNumber());
                        map.put("mobileNumber", smsSendDetails.getMobileNumber());
                        map.put("timestamp", timestamp);
                        map.put("smsFlag", "success");
                        content.add(map);
                    }
                }

            }
            if (content.isEmpty()) {
                commonResponse.setMsg("Data not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(commonResponse);
            } else {
                commonResponse.setMsg("Success");
                return ResponseEntity.status(HttpStatus.OK).body(new SmsResponse(content.size(), commonResponse.getMsg(), content));

            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
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
        int pageSize = 2;

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        Long downloadCount = documentDetailsRepo.getDownloadCount();
        Long smsCount = dataUploadRepo.getSmsCount();
        List<DataUpload> dataUpload = dataUploadRepo.findByType(pageable);
        double totalCount = dataUploadRepo.findCount();

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
            Optional<DocumentDetails> documentDetails = documentDetailsRepo.findDataByLoanNo(data.getLoanNumber(), data.getCertificateCategory());
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
        dashboardResponse.setTotalCount((long) totalCount);
        dashboardResponse.setNextPage(pageNo < totalCount / pageSize);
        dashboardResponse.setDownloadCount(downloadCount);
        commonResponse.setMsg("Data found successfully.");

        return ResponseEntity.ok(dashboardResponse);
    }

    public ResponseEntity<byte[]> fetchPdfFileForDownloadBySmsLink(String loanNo, String category) throws Exception {
        System.out.println(loanNo);
        DocumentDetails documentReader = documentDetailsRepo.findByLoanNoAndCategory(loanNo, category);

        if (documentReader == null) {
            System.out.println("File not found or invalid loanNo");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        String fileName = loanNo + ".pdf";

        if (category.contains("ADHOC")) {
            return generatePdfDocument(loanNo, fileName, projectSavePathAdhoc);

        } else if (category.contains("SOA")) {
            return generatePdfDocument(loanNo, fileName, projectSavePathSoa);

        } else if (category.contains("INTEREST_CERTIFICATE")) {
            return generatePdfDocument(loanNo, fileName, projectSavePathInterestCertificate);


        } else if (category.contains("Reminder_Payment")) {
            return generatePdfDocument(loanNo, fileName, projectSavePathPaymentReminder);

        }

        return null;
    }

    private ResponseEntity<byte[]> generatePdfDocument(String loanNo, String fileName, String projectSavePathPaymentReminder) throws IOException {
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
        documentDetailsRepo.updateDownloadCountBySmsLink(loanNo, Timestamp.valueOf(LocalDateTime.now()));

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

                unsendSmsDetails = dataUploadRepo.findBySmsCategoryForUnsendSms(smsCategory, pageable);
                detailOfCount = dataUploadRepo.findUnsendSmsCountByCategory(smsCategory);
            }

            if (!unsendSmsDetails.isEmpty() && detailOfCount > 0) {
                for (DataUpload userDetail : unsendSmsDetails) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("loanNumber", userDetail.getLoanNumber());
                    map.put("mobileNumber", userDetail.getMobileNumber());
                    map.put("timestamp", timeStamp);
                    map.put("smsFlag", "un-send");
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
package com.bulkSms.Utility;

import com.bulkSms.Entity.DocumentDetails;
import com.bulkSms.Entity.JobAuditTrail;
import com.bulkSms.Model.CommonResponse;
import com.bulkSms.Model.ListResponse;
import com.bulkSms.Model.ResponseOfFetchPdf;
import com.bulkSms.Repository.DocumentDetailsRepo;
import com.bulkSms.Repository.JobAuditTrailRepo;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class FetchAdhocUtility {

    @Autowired
    private JobAuditTrailRepo jobAuditTrailRepo;

    @Autowired
    private DocumentDetailsRepo documentDetailsRepo;

    public Object fetchAdhocCategoryfiles(String folderPath, JobAuditTrail jobAuditTrail,String category,String copyPath) {
        CommonResponse commonResponse = new CommonResponse();
        ResponseOfFetchPdf response = new ResponseOfFetchPdf();
        List<DocumentDetails> documentReaderList = new ArrayList<>();
        long count = 0L;

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
                    DocumentDetails documentReader = new DocumentDetails();

                    documentReader.setJobId(jobAuditTrail.getJobId());
                    documentReader.setFileName(String.valueOf(subDir.getFileName()));
                    documentReader.setUploadedTime(Timestamp.valueOf(LocalDateTime.now()));
                    documentReader.setCategory(category);
                    documentReader.setDownloadCount(0L);
                    documentReaderList.add(documentReader);
                    count++;
                    documentDetailsRepo.save(documentReader);

                    jobAuditTrailRepo.updateEndStatus("Number of files saved into bucket: " + count, "complete", Timestamp.valueOf(LocalDateTime.now()), jobAuditTrail.getJobId());
                    commonResponse.setMsg("All PDF files copied successfully for ADHOC.");
                    System.out.println("hello ");

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
}

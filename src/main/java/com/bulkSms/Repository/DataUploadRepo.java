package com.bulkSms.Repository;

import com.bulkSms.Entity.DataUpload;
import com.bulkSms.Model.DashboardDataList;
import com.bulkSms.Model.GetDataForSendSms;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DataUploadRepo extends JpaRepository<DataUpload, Long> {

    @Query("SELECT new com.bulkSms.Model.GetDataForSendSms(" +
            "d.id,d.loanNumber, d.mobileNumber, d.certificateCategory, " +
            "dd.file_sequence) " +
            "FROM DataUpload d " +
            "INNER JOIN DocumentDetails dd ON d.loanNumber = dd.fileName " +
            "AND d.certificateCategory = dd.category And d.upload_date=DATE(dd.uploadedTime)" +
            "LEFT JOIN BulkSms sd ON d.id =  sd.dataUpload.id " +
            "WHERE d.smsFlag='N' and d.certificateCategory=:smsCategory")
    Page<GetDataForSendSms> findByCategoryAndSmsFlagNotSent(String smsCategory, Pageable pageable);

    @Query("select d from DataUpload d where d.certificateCategory = :smsCategory and d.smsFlag = 'Y'")
    List<DataUpload> findBySmsCategoryOfSendSms(String smsCategory, Pageable pageable);

    @Query("SELECT COUNT(d.id), d.certificateCategory FROM DataUpload d WHERE d.smsFlag = 'Y' GROUP BY d.certificateCategory")
    List<Object[]> countSmsByCategory();

    @Query("select e from DataUpload e where e.loanNumber =:fileName")
    Optional<DataUpload> findByLoanNo(String fileName);

    @Query("SELECT new com.bulkSms.Model.DashboardDataList(" +
            "d.loanNumber, d.mobileNumber, d.certificateCategory, " +
            "dd.downloadCount, dd.lastDownload, sd.smsTimeStamp) " +
            "FROM DataUpload d " +
            "INNER JOIN DocumentDetails dd ON d.loanNumber = dd.fileName " +
            "AND d.certificateCategory = dd.category And d.upload_date=DATE(dd.uploadedTime) " +
            "LEFT JOIN BulkSms sd ON d.id =  sd.dataUpload.id " +
            "WHERE dd.downloadCount > 0")
    List<DashboardDataList> findByType(Pageable pageable);

    @Query("select d from DataUpload d where d.smsFlag = 'Y'")
    List<DataUpload> findByTypeOfSendSms(Pageable pageable);

    @Query("select d from DataUpload d where d.smsFlag = 'N' and d.loanNumber in (select e.fileName from DocumentDetails e)")
    List<DataUpload> findByTypeForUnsendSms(Pageable pageable);

    @Query("SELECT d\n" +
            "FROM DataUpload d\n" +
            "INNER JOIN DocumentDetails dd ON d.loanNumber = dd.fileName \n" +
            "    AND d.certificateCategory = dd.category \n" +
            "    AND d.upload_date = DATE(dd.uploadedTime)\n" +
            "LEFT JOIN BulkSms sd ON d.id = sd.dataUpload.id\n" +
            "WHERE d.smsFlag = 'N' \n" +
            "   AND d.certificateCategory = :smsCategory")
    List<DataUpload> findBySmsCategoryForUnsendSms(String smsCategory, Pageable pageable);

    @Query("select count(d) from DataUpload d \n" +
            "inner join DocumentDetails dd \n" +
            "on d.loanNumber = dd.fileName \n" +
            "and d.certificateCategory = dd.category \n" +
            "where dd.downloadCount > 0")
    long listTotalDownloadCount();

    @Query("select count(d) from DataUpload d where d.smsFlag = 'Y'")
    long findCount();

    @Query("select count(d) from DataUpload d where d.certificateCategory = :smsCategory and d.smsFlag = 'Y'")
    long findCountWithSmsCategory(String smsCategory);

    @Query("select count(d) from DataUpload d where d.smsFlag = 'N'  and d.loanNumber in (select e.fileName from DocumentDetails e)")
    long findUnsendSmsCountByType();

    @Query("select count(d) from DataUpload d where d.certificateCategory = :smsCategory and d.smsFlag = 'N'  and d.loanNumber in (select e.fileName from DocumentDetails e where e.category=:smsCategory)")
    long findUnsendSmsCountByCategory(String smsCategory);

    @Query("SELECT d FROM DataUpload d WHERE d.loanNumber = :loanNumber and d.certificateCategory = :smsCategory")
    Optional<DataUpload> findByloanNumber(String loanNumber, String smsCategory);
}
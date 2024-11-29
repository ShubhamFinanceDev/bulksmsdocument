package com.bulkSms.Model;

import lombok.Data;
@Data
public class GetDataForSendSms {
        private  Long id;
        private String loanNumber;
        private String mobileNumber;
        private String certificateCategory;
        private Long fileSequenceNo;

        public GetDataForSendSms(Long id,String loanNumber, String mobileNumber, String certificateCategory,Long fileSequenceNo) {
            this.id=id;
            this.loanNumber = loanNumber;
            this.mobileNumber = mobileNumber;
            this.certificateCategory = certificateCategory;
            this.fileSequenceNo=fileSequenceNo;
        }

        public GetDataForSendSms() {}
    }

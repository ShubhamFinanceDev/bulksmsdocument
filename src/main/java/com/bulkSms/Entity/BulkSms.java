package com.bulkSms.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "bulk_sms")
@Data
public class BulkSms {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id")
    private long userId;

    @Column(name = "sms_timeStamp")
    private LocalDateTime smsTimeStamp;

    @JsonIgnore
    @JoinColumn(name = "id")
    @OneToOne
    private DataUpload dataUpload;
}

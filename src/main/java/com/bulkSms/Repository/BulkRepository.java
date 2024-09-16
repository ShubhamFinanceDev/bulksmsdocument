package com.bulkSms.Repository;

import com.bulkSms.Entity.BulkSms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BulkRepository extends JpaRepository<BulkSms,Long> {
}

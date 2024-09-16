package com.bulkSms.Repository;


import com.bulkSms.Entity.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDetailRepo extends JpaRepository<UserDetail,Long> {
    @Query("select u from UserDetail u where u.emailId=:userName")
    Optional<UserDetail> findUser(String userName);

    @Query("select u from UserDetail u where u.emailId=:emailId")
    Optional<Object> findByEmailId(String emailId);
}

package com.bulkSms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class BulkSmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(BulkSmsApplication.class, args);
	}

}

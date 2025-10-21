package com.example.ARDU;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // New Import

@SpringBootApplication
@EnableScheduling // Add this annotation to enable the scheduler for ArchivingService
public class ArduApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArduApplication.class, args);
	}

}
package com.pet_project.watchstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class WatchstoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(WatchstoreApplication.class, args);
	}

}

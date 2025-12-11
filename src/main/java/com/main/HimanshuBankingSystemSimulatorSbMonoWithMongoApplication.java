package com.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = "com")
@EnableMongoRepositories(basePackages = "com.repository")
public class HimanshuBankingSystemSimulatorSbMonoWithMongoApplication {

	public static void main(String[] args) {
		SpringApplication.run(HimanshuBankingSystemSimulatorSbMonoWithMongoApplication.class, args);
        System.out.println("SpringBoot Monolithic banking system simulator  using mongodb");
	}

}

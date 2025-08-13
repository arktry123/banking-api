package com.eagle.banking;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = "com.eagle.banking")
public class BankingApplication {

	public static void main(String[] args) {
		SpringApplicationBuilder appBuilder = new SpringApplicationBuilder(BankingApplication.class);
		appBuilder.run(args);
	}

}

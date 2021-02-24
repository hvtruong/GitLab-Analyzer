package com.haumea.gitanalyzer;

import org.jasig.cas.client.boot.configuration.EnableCasClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableCasClient
public class GitanalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GitanalyzerApplication.class, args);
	}

}

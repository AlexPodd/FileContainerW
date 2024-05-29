package com.DBAuthExample.AuthExample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan("com.DBAuthExample.AuthExample.Storage")
@SpringBootApplication

public class AuthExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthExampleApplication.class, args);
	}

}

package com.shruti.springsecurityclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
public class SpringSecurityClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringSecurityClientApplication.class, args);
	}

}

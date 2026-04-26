package com.clm.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class ClmBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClmBackendApplication.class, args);
	}

}

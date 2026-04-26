package com.clm.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfiguration {

	@Bean
	OpenAPI clmOpenApi() {
		return new OpenAPI()
			.info(new Info()
				.title("CLM Platform API")
				.version("v1")
				.description("Certificate lifecycle management platform API foundation."));
	}
}

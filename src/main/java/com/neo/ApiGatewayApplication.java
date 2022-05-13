package com.neo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.neo.branding.NeoBanner;

@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(ApiGatewayApplication.class);
		springApplication.setBanner(new NeoBanner());
		springApplication.run(args);
	}
}
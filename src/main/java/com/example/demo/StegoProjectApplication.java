package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;


//mvnw clean package
//java -jar demo-0.0.1-SNAPSHOT.jar
@SpringBootApplication
public class StegoProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(StegoProjectApplication.class, args);
	}

}

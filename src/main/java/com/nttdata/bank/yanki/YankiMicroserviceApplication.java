package com.nttdata.bank.yanki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class YankiMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(YankiMicroserviceApplication.class, args);
	}

}

package com.Utc.RutaExpress;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RutaExpressApplication {

	public static void main(String[] args) {
		SpringApplication.run(RutaExpressApplication.class, args);
	}

}

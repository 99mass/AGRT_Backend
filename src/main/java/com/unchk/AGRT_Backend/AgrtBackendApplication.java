package com.unchk.AGRT_Backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.unchk.AGRT_Backend.models")
public class AgrtBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgrtBackendApplication.class, args);
	}

}

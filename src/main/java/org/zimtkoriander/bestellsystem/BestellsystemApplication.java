package org.zimtkoriander.bestellsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@AutoConfigurationPackage(basePackages = {"org.zimtkoriander.bestellsystem", "Model"})
public class BestellsystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(BestellsystemApplication.class, args);
	}

}

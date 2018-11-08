package net.atpco.hack.journeytransformer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration;
import org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;

@SpringBootApplication(exclude={JmxAutoConfiguration.class, SpringApplicationAdminJmxAutoConfiguration.class, 
		IntegrationAutoConfiguration.class})
public class TransformerApp {

	public static void main(String[] args) {
		SpringApplication.run(TransformerApp.class, args);
	}
}

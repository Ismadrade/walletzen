package br.com.walletzen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WzUserApplication {

	public static void main(String[] args) {
		SpringApplication.run(WzUserApplication.class, args);
	}

}

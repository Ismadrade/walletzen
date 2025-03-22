package br.com.walletzen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class WzServiceRegistryApplication {

	public static void main(String[] args) {
		SpringApplication.run(WzServiceRegistryApplication.class, args);
	}

}

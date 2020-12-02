package io.illuminates.communications.server;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class TalkIlluminatesIntegrationApplication {
	
    public static void main(String[] args) {
        SpringApplication.run(TalkIlluminatesIntegrationApplication.class, args);
    }
}

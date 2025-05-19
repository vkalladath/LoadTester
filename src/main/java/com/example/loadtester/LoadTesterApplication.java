package com.example.loadtester;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Enable Spring's scheduling capabilities
public class LoadTesterApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoadTesterApplication.class, args);
    }

}

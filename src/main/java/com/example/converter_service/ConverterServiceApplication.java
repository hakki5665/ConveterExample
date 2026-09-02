package com.example.converter_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ConverterServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConverterServiceApplication.class, args);
    }
}

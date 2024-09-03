package org.example.customerservice;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class CustomerApplication {
    public static void main(String[] args) {

        SpringApplication.run(CustomerApplication.class);
    }
}
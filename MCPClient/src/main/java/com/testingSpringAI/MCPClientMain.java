package com.testingSpringAI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.testingSpringAI")
public class MCPClientMain {
    public static void main(String[] args) {
        SpringApplication.run(MCPClientMain.class, args);
        System.out.println("Hello, MCPClientMain World!");
    }
}
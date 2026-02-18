package com.testing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.testing")
public class MCPServerMain {
    public static void main(String[] args) {
        SpringApplication.run(MCPServerMain.class, args);
        System.out.println("Hello, MCPServerMain World!");
    }
}
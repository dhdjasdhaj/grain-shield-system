package com.grainshield;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GrainShieldApplication {
    public static void main(String[] args) {
        SpringApplication.run(GrainShieldApplication.class, args);
    }
}

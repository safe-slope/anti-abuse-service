package io.github.safeslope.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "io.github.safeslope")
public class AntiAbuseApplication {
    public static void main(String[] args) {
        SpringApplication.run(AntiAbuseApplication.class, args);
    }
}

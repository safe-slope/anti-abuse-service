package io.github.safeslope.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "io.github.safeslope")
@EnableJpaRepositories(basePackages = "io.github.safeslope")
@EntityScan(basePackages = "io.github.safeslope.entities")
public class AntiAbuseApplication {
    public static void main(String[] args) {
        SpringApplication.run(AntiAbuseApplication.class, args);
    }
}

package com.yaprj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class YaPrjApplication {
    public static void main(String[] args) {
        SpringApplication.run(YaPrjApplication.class, args);
    }
}

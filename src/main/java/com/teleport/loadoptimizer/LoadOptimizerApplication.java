package com.teleport.loadoptimizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class LoadOptimizerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoadOptimizerApplication.class, args);
    }
}

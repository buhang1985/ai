package com.gjyy.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class AiApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AiApplication.class, args);
        System.out.println("DataSource URL: " + context.getEnvironment().getProperty("spring.datasource.url"));
        System.out.println("DataSource Username: " + context.getEnvironment().getProperty("spring.datasource.username"));
        System.out.println("DataSource Driver: " + context.getEnvironment().getProperty("spring.datasource.driver-class-name"));
        SpringApplication.run(AiApplication.class, args);


    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
//    @Bean
//    public JdbcTemplate jdbcTemplate() {
//        return new JdbcTemplate();
//    }
}

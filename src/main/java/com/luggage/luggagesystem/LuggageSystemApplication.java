package com.luggage.luggagesystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.luggage.luggagesystem.mapper")
public class LuggageSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuggageSystemApplication.class, args);
    }

}

package com.minitiktok;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.minitiktok.mapper")
public class MiniTikTokApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniTikTokApplication.class, args);
    }
}

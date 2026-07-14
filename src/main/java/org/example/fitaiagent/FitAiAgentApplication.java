package org.example.fitaiagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("org.example.fitaiagent.mapper") // 确保扫描正确
@EnableScheduling
public class FitAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitAiAgentApplication.class, args);
    }

}

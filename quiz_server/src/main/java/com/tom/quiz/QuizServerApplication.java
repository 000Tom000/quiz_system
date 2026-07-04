package com.tom.quiz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.tom.quiz.mapper")
public class QuizServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizServerApplication.class, args);
    }

}

package com.pairstudy;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
@MapperScan("com.pairstudy.mapper")
public class PairStudyApplication {
    public static void main(String[] args) { SpringApplication.run(PairStudyApplication.class, args); }
}

package com.tencent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.tencent")
@MapperScan(basePackages = {"com.tencent.dao"})
public class QhrServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(QhrServiceApplication.class, args);
  }
}

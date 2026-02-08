package com.tencent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = {"com.tencent.dao"})
public class WxCloudRunApplication {  

  static void main(String[] args) {
    SpringApplication.run(WxCloudRunApplication.class, args);
  }
}

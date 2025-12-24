package com.fw.irongate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IronGate {

  public static void main(String[] args) {
    SpringApplication.run(IronGate.class, args);
  }
}

package edu.university.lab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("edu.university.lab.module.**.mapper")
public class LabManagementBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(LabManagementBackendApplication.class, args);
	}

}

package com.one.school;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.one.school")
@MapperScan("com.one.school.dao")
@SpringBootApplication
public class OsmsApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(OsmsApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(OsmsApplication.class);
	}
}

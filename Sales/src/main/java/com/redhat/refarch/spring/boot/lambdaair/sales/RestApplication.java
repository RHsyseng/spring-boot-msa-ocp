package com.redhat.refarch.spring.boot.lambdaair.sales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestApplication
{
	public static void main(String[] args)
	{
		SpringApplication.run( RestApplication.class, args );
	}
}
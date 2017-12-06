package com.redhat.refarch.spring.boot.lambdaair.airports.service;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ApplicationInitialization implements ApplicationListener<ContextRefreshedEvent>
{
	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent)
	{
		try
		{
			AirportsService.loadAirports();
		}
		catch( IOException e )
		{
			throw new IllegalStateException( e );
		}
	}
}

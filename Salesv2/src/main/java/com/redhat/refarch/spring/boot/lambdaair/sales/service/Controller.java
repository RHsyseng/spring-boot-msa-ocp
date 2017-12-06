package com.redhat.refarch.spring.boot.lambdaair.sales.service;

import com.redhat.refarch.spring.boot.lambdaair.sales.model.Flight;
import com.redhat.refarch.spring.boot.lambdaair.sales.model.Itinerary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

@RestController
public class Controller
{
	private static Logger logger = Logger.getLogger( Controller.class.getName() );
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern( "yyyyMMdd" );

	@Autowired
	private Tracer tracer;

	@RequestMapping( value = "/price", method = RequestMethod.POST )
	public Itinerary price(@RequestBody Flight flight)
	{
		tracer.addTag( "Operation", "Determine Price for a Flight" );
		Itinerary itinerary = SalesTicketingService.price( flight );
		logger.info("Priced ticket at " + itinerary.getPrice() + " with lower hop discount");
		return itinerary;
	}
}
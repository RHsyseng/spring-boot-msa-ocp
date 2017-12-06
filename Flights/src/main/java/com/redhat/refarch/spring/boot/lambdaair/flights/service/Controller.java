package com.redhat.refarch.spring.boot.lambdaair.flights.service;

import com.redhat.refarch.spring.boot.lambdaair.flights.model.Airport;
import com.redhat.refarch.spring.boot.lambdaair.flights.model.Flight;
import com.redhat.refarch.spring.boot.lambdaair.flights.model.FlightSchedule;
import com.redhat.refarch.spring.boot.lambdaair.flights.model.FlightSegment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RibbonClient( name = "airports" )
public class Controller
{
	private static Logger logger = Logger.getLogger( Controller.class.getName() );
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern( "yyyyMMdd" );

	@LoadBalanced
	@Bean
	RestTemplate restTemplate()
	{
		return new RestTemplate();
	}

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private Tracer tracer;

	@RequestMapping( value = "/query", method = RequestMethod.GET )
	public List<Flight> query(@RequestParam( "date" ) String date, @RequestParam( "origin" ) String origin, @RequestParam( "destination" ) String destination)
	{
		tracer.addTag( "Operation", "Look Up Flights" );
		Map<String, Airport> airports = new HashMap<>();
		Airport[] airportArray = restTemplate.getForObject( "http://zuul/airports/airports", Airport[].class );
		for( Airport airport : airportArray )
		{
			airports.put( airport.getCode(), airport );
		}

		LocalDate travelDate = LocalDate.parse( date, dateFormatter );
		logger.info( origin + " => " + destination + " on " + travelDate );
		List<FlightSchedule[]> routes = FlightSchedulingService.getRoutes( airports, origin, destination );
		List<Flight> flights = new ArrayList<>();
		for( FlightSchedule[] route : routes )
		{
			FlightSegment[] segments = new FlightSegment[route.length];
			for( int index = 0; index < segments.length; index++ )
			{
				segments[index] = new FlightSegment();
				segments[index].setFlightNumber( Integer.parseInt( route[index].getFlightNumber() ) );
				segments[index].setDepartureAirport( route[index].getDepartureAirport() );
				segments[index].setArrivalAirport( route[index].getArrivalAirport() );
				//For now assume all travel time is for the requested date and not +1.
				segments[index].setDepartureTime( getInstant( travelDate, route[index].getDepartureTime(), airports.get( route[index].getDepartureAirport() ).getZoneId() ) );
				segments[index].setArrivalTime( getInstant( travelDate, route[index].getArrivalTime(), airports.get( route[index].getArrivalAirport() ).getZoneId() ) );
			}
			//Fix the timestamp when date is the next morning
			Instant previousTimestamp = segments[0].getDepartureTime();
			for( FlightSegment segment : segments )
			{
				if( previousTimestamp.isAfter( segment.getDepartureTime() ) )
				{
					segment.setDepartureTime( segment.getDepartureTime().plus( 1, ChronoUnit.DAYS ) );
				}
				previousTimestamp = segment.getDepartureTime();
				if( previousTimestamp.isAfter( segment.getArrivalTime() ) )
				{
					segment.setArrivalTime( segment.getArrivalTime().plus( 1, ChronoUnit.DAYS ) );
				}
				previousTimestamp = segment.getArrivalTime();
			}
			Flight flight = new Flight();
			flight.setSegments( segments );
			flights.add( flight );
		}
		for( Flight flight : flights )
		{
			if( flight.getSegments().length == 1 )
			{
				logger.info( "Nonstop:\t" + flight.getSegments()[0] );
			}
			else
			{
				logger.info( "One stop\n\t" + flight.getSegments()[0] + "\n\t" + flight.getSegments()[1] );
			}
		}
		return flights;
	}

	private Instant getInstant(LocalDate travelDate, LocalTime localTime, ZoneId zoneId)
	{
		return ZonedDateTime.of( travelDate, localTime, zoneId ).toInstant();
	}
}
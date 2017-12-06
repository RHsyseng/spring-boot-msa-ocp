package com.redhat.refarch.spring.boot.lambdaair.sales.service;

import com.redhat.refarch.spring.boot.lambdaair.sales.model.Flight;
import com.redhat.refarch.spring.boot.lambdaair.sales.model.FlightSegment;
import com.redhat.refarch.spring.boot.lambdaair.sales.model.Itinerary;
import com.redhat.refarch.spring.boot.lambdaair.sales.model.Pricing;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

class SalesTicketingService
{
	private static Map<Integer, Pricing> pricingMap = new HashMap<>();
	private static final double extraHopDiscount = 0.8;
	private static final double layoverDiscount = 0.9;

	static void loadPricing() throws IOException
	{
		if( pricingMap.isEmpty() )
		{
			InputStream inputStream = SalesTicketingService.class.getResourceAsStream( "/pricing.csv" );
			CSVReader reader = new CSVReader( new InputStreamReader( inputStream ), '\t' );
			String[] nextLine;
			while( (nextLine = reader.readNext()) != null )
			{
				Pricing pricing = new Pricing();
				pricing.flightNumber = Integer.parseInt( nextLine[0] );
				pricing.basePrice = Integer.parseInt( nextLine[1] );
				pricing.availability = new int[nextLine.length - 2];
				for( int index = 2; index < nextLine.length; index++ )
				{
					pricing.availability[index - 2] = Integer.parseInt( nextLine[index] );
				}
				pricingMap.put( pricing.flightNumber, pricing );
			}
		}
	}

	static Itinerary price(Flight flight)
	{
		int dateOffset = getDateOffset( flight.getSegments()[0].getDepartureTime() );
		int price = 0;
		Instant arrival = null;
		for( FlightSegment flightSegment : flight.getSegments() )
		{
			Pricing pricing = pricingMap.get( flightSegment.getFlightNumber() );
			int segmentPrice = pricing.basePrice;
			segmentPrice *= getAvailabilityDiscount( dateOffset, pricing.availability[dateOffset - 1] );
			if( price == 0 )
			{
				price = segmentPrice;
			}
			else
			{
				int hopPrice = segmentPrice;
				int layover = (int)Duration.between( arrival, flightSegment.getDepartureTime() ).toHours() - 1;
				if( layover > 0 )
				{
					//Apply a discount for every extra hour of layover:
					hopPrice *= layover * layoverDiscount;
				}
				//Add to total price so far:
				price += hopPrice;
				//Apply another discount on top of total for extra hop:
				price *= extraHopDiscount;
			}
			arrival = flightSegment.getArrivalTime();
		}
		Itinerary itinerary = new Itinerary( flight );
		itinerary.setPrice( price );
		return itinerary;
	}

	private static double getAvailabilityDiscount(int dateOffset, int availability)
	{
		if( dateOffset <= 3 )
		{
			if( availability > 60 )
			{
				return 0.7;
			}
			else if( availability > 50 )
			{
				return 0.8;
			}
			else
			{
				return 1;
			}
		}
		else if( dateOffset <= 7 )
		{
			if( availability > 70 )
			{
				return 0.75;
			}
			else
			{
				return 1;
			}
		}
		else if( dateOffset <= 21 )
		{
			if( availability > 90 )
			{
				return 0.6;
			}
			else if( availability > 80 )
			{
				return 0.8;
			}
			else
			{
				return 1;
			}
		}
		else
		{
			if( availability > 90 )
			{
				return 0.9;
			}
			else
			{
				return 1;
			}
		}
	}

	private static int getDateOffset(Instant instant)
	{
		int dateOffset = (int)Duration.between( Instant.now(), instant ).toDays();
		if( dateOffset < 1 )
		{
			dateOffset = 1;
		}
		else if( dateOffset > 60 )
		{
			dateOffset = 60;
		}
		return dateOffset;
	}
}
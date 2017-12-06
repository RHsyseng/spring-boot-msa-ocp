package com.redhat.refarch.spring.boot.lambdaair.airports.service;

import com.redhat.refarch.spring.boot.lambdaair.airports.model.Airport;
import com.redhat.refarch.spring.boot.lambdaair.airports.model.Coordinates;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import au.com.bytecode.opencsv.CSVReader;

public class AirportsService
{
	private static Logger logger = Logger.getLogger( Controller.class.getName() );
	private static Map<String, Airport> airports = new HashMap<>();

	public static void loadAirports() throws IOException
	{
		if( airports.isEmpty() )
		{
			InputStream inputStream = AirportsService.class.getResourceAsStream( "/airports.csv" );
			CSVReader reader = new CSVReader( new InputStreamReader( inputStream ), '\t' );
			String[] nextLine;
			while( (nextLine = reader.readNext()) != null )
			{
				Airport airport = new Airport();
				airport.setName( nextLine[0] );
				airport.setCity( nextLine[1] );
				airport.setCountry( nextLine[2] );
				airport.setCode( nextLine[3] );
				airport.setCoordinates( new Coordinates(nextLine[5], nextLine[6], nextLine[7] ) );
				airport.setZoneId( nextLine[10] );
				addAirport( airport );
			}
			logger.info( "Populated " + airports.size() + " airports" );
		}
	}

	private static void addAirport(Airport airport)
	{
		airports.put( airport.getCode(), airport );
	}

	public static Airport getAirport(String code)
	{
		Airport airport = airports.get( code );
		logger.info( "Got " + airport + " for " + code );
		return airport;
	}

	public static Collection<Airport> getAirports()
	{
		return Collections.unmodifiableCollection( airports.values() );
	}

	public static Set<String> getAirportCodes()
	{
		return Collections.unmodifiableSet( airports.keySet() );
	}

	public static Collection<Airport> filter(String filter)
	{
		List<Airport> results = new ArrayList<>();
		logger.info( "Will filter airports for " + filter );
		filter = filter.toUpperCase( Locale.US );
		for( Airport airport : airports.values() )
		{
			if( airport.getCity().toUpperCase( Locale.US ).startsWith( filter ) )
			{
				results.add( airport );
			}
		}
		results.sort( Airport.cityNameComparator );
		logger.info( "Returning " + results );
		return results;
	}
}
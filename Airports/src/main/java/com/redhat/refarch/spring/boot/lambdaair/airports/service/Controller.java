package com.redhat.refarch.spring.boot.lambdaair.airports.service;

import com.redhat.refarch.spring.boot.lambdaair.airports.model.Airport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Locale;

@RestController
public class Controller
{
	@Autowired
	private Tracer tracer;

	@RequestMapping( value = "/airports", method = RequestMethod.GET )
	public Collection<Airport> airports(@RequestParam( value = "filter", required = false ) String filter)
	{
		tracer.addTag( "Operation", "Look Up Airports" );
		if( StringUtils.isEmpty( filter ) )
		{
			return AirportsService.getAirports();
		}
		else
		{
			return AirportsService.filter( filter );
		}
	}

	@RequestMapping( value = "/airports/{code}", method = RequestMethod.GET )
	public Airport getAirport(@PathVariable( "code" ) String code)
	{
		tracer.addTag( "Operation", "Look Up Single Airport" );
		return AirportsService.getAirport( code.toUpperCase( Locale.US ) );
	}
}
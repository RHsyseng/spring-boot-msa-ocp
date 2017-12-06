package com.redhat.refarch.spring.boot.lambdaair.flights.model;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;

public class Flight
{
	private FlightSegment[] segments;
	private int duration;

	public FlightSegment[] getSegments()
	{
		return segments;
	}

	public void setSegments(FlightSegment... segments)
	{
		this.segments = segments;
		duration = (int)ChronoUnit.MINUTES.between( segments[0].getDepartureTime(), segments[segments.length - 1].getArrivalTime() );
	}

	public int getDuration()
	{
		return duration;
	}

	@Override
	public boolean equals(Object o)
	{
		if( this == o )
		{
			return true;
		}
		if( o == null || getClass() != o.getClass() )
		{
			return false;
		}

		Flight flight = (Flight)o;

		if( duration != flight.duration )
		{
			return false;
		}
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		return Arrays.equals( segments, flight.segments );
	}

	@Override
	public int hashCode()
	{
		int result = Arrays.hashCode( segments );
		result = 31 * result + duration;
		return result;
	}

	@Override
	public String toString()
	{
		return "Flight{" + "segments=" + Arrays.toString( segments ) + ", duration=" + duration + '}';
	}
}
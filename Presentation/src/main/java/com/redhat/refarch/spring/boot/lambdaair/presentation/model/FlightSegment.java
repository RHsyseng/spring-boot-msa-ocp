package com.redhat.refarch.spring.boot.lambdaair.presentation.model;

import java.time.Instant;

public class FlightSegment
{
	private int flightNumber;
	private String departureAirport;
	private Instant departureTime;
	private String arrivalAirport;
	private Instant arrivalTime;
	private String formattedDepartureTime;
	private String formattedArrivalTime;

	public int getFlightNumber()
	{
		return flightNumber;
	}

	public void setFlightNumber(int flightNumber)
	{
		this.flightNumber = flightNumber;
	}

	public String getDepartureAirport()
	{
		return departureAirport;
	}

	public void setDepartureAirport(String departureAirport)
	{
		this.departureAirport = departureAirport;
	}

	public Instant getDepartureTime()
	{
		return departureTime;
	}

	public void setDepartureTime(Instant departureTime)
	{
		this.departureTime = departureTime;
	}

	public String getArrivalAirport()
	{
		return arrivalAirport;
	}

	public void setArrivalAirport(String arrivalAirport)
	{
		this.arrivalAirport = arrivalAirport;
	}

	public Instant getArrivalTime()
	{
		return arrivalTime;
	}

	public void setArrivalTime(Instant arrivalTime)
	{
		this.arrivalTime = arrivalTime;
	}

	public String getFormattedDepartureTime()
	{
		return formattedDepartureTime;
	}

	public void setFormattedDepartureTime(String formattedDepartureTime)
	{
		this.formattedDepartureTime = formattedDepartureTime;
	}

	public String getFormattedArrivalTime()
	{
		return formattedArrivalTime;
	}

	public void setFormattedArrivalTime(String formattedArrivalTime)
	{
		this.formattedArrivalTime = formattedArrivalTime;
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

		FlightSegment segment = (FlightSegment)o;

		if( flightNumber != segment.flightNumber )
		{
			return false;
		}
		if( departureAirport != null ? !departureAirport.equals( segment.departureAirport ) : segment.departureAirport != null )
		{
			return false;
		}
		if( departureTime != null ? !departureTime.equals( segment.departureTime ) : segment.departureTime != null )
		{
			return false;
		}
		if( arrivalAirport != null ? !arrivalAirport.equals( segment.arrivalAirport ) : segment.arrivalAirport != null )
		{
			return false;
		}
		if( arrivalTime != null ? !arrivalTime.equals( segment.arrivalTime ) : segment.arrivalTime != null )
		{
			return false;
		}
		if( formattedDepartureTime != null ? !formattedDepartureTime.equals( segment.formattedDepartureTime ) : segment.formattedDepartureTime != null )
		{
			return false;
		}
		return formattedArrivalTime != null ? formattedArrivalTime.equals( segment.formattedArrivalTime ) : segment.formattedArrivalTime == null;
	}

	@Override
	public int hashCode()
	{
		int result = flightNumber;
		result = 31 * result + (departureAirport != null ? departureAirport.hashCode() : 0);
		result = 31 * result + (departureTime != null ? departureTime.hashCode() : 0);
		result = 31 * result + (arrivalAirport != null ? arrivalAirport.hashCode() : 0);
		result = 31 * result + (arrivalTime != null ? arrivalTime.hashCode() : 0);
		result = 31 * result + (formattedDepartureTime != null ? formattedDepartureTime.hashCode() : 0);
		result = 31 * result + (formattedArrivalTime != null ? formattedArrivalTime.hashCode() : 0);
		return result;
	}

	@Override
	public String toString()
	{
		return "FlightSegment{" + "flightNumber=" + flightNumber + ", departureAirport='" + departureAirport + '\'' + ", departureTime=" + departureTime + ", arrivalAirport='" + arrivalAirport + '\'' + ", arrivalTime=" + arrivalTime + ", formattedDepartureTime='" + formattedDepartureTime + '\'' + ", formattedArrivalTime='" + formattedArrivalTime + '\'' + '}';
	}
}
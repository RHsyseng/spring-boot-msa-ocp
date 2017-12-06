package com.redhat.refarch.spring.boot.lambdaair.sales.model;

import java.time.LocalTime;

public class FlightSchedule
{
	private String flightNumber;
	private String departureAirport;
	private LocalTime departureTime;
	private String arrivalAirport;
	private LocalTime arrivalTime;

	public String getDepartureAirport()
	{
		return departureAirport;
	}

	public void setDepartureAirport(String departureAirport)
	{
		this.departureAirport = departureAirport;
	}

	public LocalTime getDepartureTime()
	{
		return departureTime;
	}

	public void setDepartureTime(LocalTime departureTime)
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

	public LocalTime getArrivalTime()
	{
		return arrivalTime;
	}

	public void setArrivalTime(LocalTime arrivalTime)
	{
		this.arrivalTime = arrivalTime;
	}

	public String getFlightNumber()
	{
		return flightNumber;
	}

	public void setFlightNumber(String flightNumber)
	{
		this.flightNumber = flightNumber;
	}

	@Override
	public String toString()
	{
		return "FlightSchedule{" + "flightNumber='" + flightNumber + '\'' + ", departureAirport='" + departureAirport + '\'' + ", departureTime=" + departureTime + ", arrivalAirport='" + arrivalAirport + '\'' + ", arrivalTime=" + arrivalTime + '}';
	}
}
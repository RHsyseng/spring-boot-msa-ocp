package com.redhat.refarch.spring.boot.lambdaair.airports.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.logging.Logger;

public class Coordinates implements Serializable
{
	private static Logger logger = Logger.getLogger( Coordinates.class.getName() );

	private static final long serialVersionUID = 1L;
	private static final double R = 3959.87; // In miles
	private double latitude;
	private double longitude;
	private double altitude;

	public Coordinates(double latitude, double longitude, double altitude)
	{
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
	}

	public Coordinates(String latitude, String longitude, String altitude) throws NumberFormatException
	{
		this.latitude = Double.parseDouble( latitude );
		this.longitude = Double.parseDouble( longitude );
		this.altitude = Double.parseDouble( altitude );
	}

	public static Coordinates fromString(String coordinateString)
	{
		int firstSeparatorIndex = coordinateString.indexOf( ',' );
		int lastSeparatorIndex = coordinateString.lastIndexOf( ',' );
		String latitude = coordinateString.substring( 0, firstSeparatorIndex );
		String longitude = coordinateString.substring( firstSeparatorIndex + 1, lastSeparatorIndex );
		String altitude = coordinateString.substring( lastSeparatorIndex + 1, coordinateString.length() );
		return new Coordinates( latitude, longitude, altitude );
	}

	public double getLatitude()
	{
		return latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}

	public double getAltitude()
	{
		return altitude;
	}

	public double distance(Coordinates coordinates)
	{
		double dLat = Math.toRadians( coordinates.latitude - latitude );
		double dLon = Math.toRadians( coordinates.longitude - longitude );
		double lat1 = Math.toRadians( latitude );
		double lat2 = Math.toRadians( coordinates.latitude );

		double a = Math.sin( dLat / 2 ) * Math.sin( dLat / 2 ) + Math.sin( dLon / 2 ) * Math.sin( dLon / 2 ) * Math.cos( lat1 ) * Math.cos( lat2 );
		double c = 2 * Math.asin( Math.sqrt( a ) );
		return R * c;
	}

	public String asString()
	{
		return latitude + "," + longitude + "," + altitude;
	}

	@Override
	public String toString()
	{
		return "Coordinates [latitude=" + latitude + ", longitude=" + longitude + ", altitude=" + altitude + "]";
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

		Coordinates that = (Coordinates)o;

		if( Double.compare( that.latitude, latitude ) != 0 )
		{
			return false;
		}
		if( Double.compare( that.longitude, longitude ) != 0 )
		{
			return false;
		}
		return Double.compare( that.altitude, altitude ) == 0;
	}

	@Override
	public int hashCode()
	{
		int result;
		long temp;
		temp = Double.doubleToLongBits( latitude );
		result = (int)(temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits( longitude );
		result = 31 * result + (int)(temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits( altitude );
		result = 31 * result + (int)(temp ^ (temp >>> 32));
		return result;
	}

	public interface HasCoordinates
	{

		Coordinates getCoordinates();
	}

	public static class DistanceComparator implements Comparator<HasCoordinates>
	{

		private Coordinates destination;
		private int compareCount = 0;

		public DistanceComparator(Coordinates destination)
		{
			logger.fine( "DistanceComparator instantiated with " + destination );
			if( destination == null )
			{
				throw new IllegalArgumentException( "Cannot sort based on distance to a null location" );
			}
			this.destination = destination;
		}

		public int getCompareCount()
		{
			return compareCount;
		}

		@Override
		public int compare(HasCoordinates lhs, HasCoordinates rhs)
		{
			int result;
			if( lhs.getCoordinates() == null )
			{
				if( rhs.getCoordinates() == null )
				{
					result = 0;
				}
				else
				{
					result = 1;
				}
			}
			else if( rhs.getCoordinates() == null )
			{
				result = -1;
			}
			else
			{
				compareCount++;
				double lhsDistance = lhs.getCoordinates().distance( destination );
				double rhsDistance = rhs.getCoordinates().distance( destination );
				double distanceDiff = lhsDistance - rhsDistance;
				if( distanceDiff > 0 )
				{
					result = 1;
				}
				else if( distanceDiff < 0 )
				{
					result = -1;
				}
				else
				{
					result = 0;
				}
			}
			return result;
		}
	}
}
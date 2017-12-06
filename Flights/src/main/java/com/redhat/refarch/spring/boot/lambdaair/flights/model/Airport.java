package com.redhat.refarch.spring.boot.lambdaair.flights.model;

import java.time.ZoneId;

public class Airport
{
	private String code;
	private ZoneId zoneId;

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public ZoneId getZoneId()
	{
		return zoneId;
	}

	public void setZoneId(String zoneId)
	{
		this.zoneId = ZoneId.of( zoneId );
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

		Airport airport = (Airport)o;

		if( code != null ? !code.equals( airport.code ) : airport.code != null )
		{
			return false;
		}
		return zoneId != null ? zoneId.equals( airport.zoneId ) : airport.zoneId == null;
	}

	@Override
	public int hashCode()
	{
		int result = code != null ? code.hashCode() : 0;
		result = 31 * result + (zoneId != null ? zoneId.hashCode() : 0);
		return result;
	}

	@Override
	public String toString()
	{
		return "Airport{" + "code='" + code + '\'' + ", zoneId=" + zoneId + '}';
	}
}
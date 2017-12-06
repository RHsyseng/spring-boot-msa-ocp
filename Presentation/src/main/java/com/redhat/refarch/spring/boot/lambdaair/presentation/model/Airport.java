package com.redhat.refarch.spring.boot.lambdaair.presentation.model;

import java.time.ZoneId;

public class Airport
{
	private String code;
	private String name;
	private String city;
	private String country;
	private ZoneId zoneId;

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getCountry()
	{
		return country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public String getZoneId()
	{
		return zoneId.getId();
	}

	public void setZoneId(String zoneId)
	{
		this.zoneId = ZoneId.of( zoneId );
	}

	public String getCity()
	{
		return city;
	}

	public void setCity(String city)
	{
		this.city = city;
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
		if( name != null ? !name.equals( airport.name ) : airport.name != null )
		{
			return false;
		}
		if( city != null ? !city.equals( airport.city ) : airport.city != null )
		{
			return false;
		}
		if( country != null ? !country.equals( airport.country ) : airport.country != null )
		{
			return false;
		}
		return zoneId != null ? zoneId.equals( airport.zoneId ) : airport.zoneId == null;
	}

	@Override
	public int hashCode()
	{
		int result = code != null ? code.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (city != null ? city.hashCode() : 0);
		result = 31 * result + (country != null ? country.hashCode() : 0);
		result = 31 * result + (zoneId != null ? zoneId.hashCode() : 0);
		return result;
	}

	@Override
	public String toString()
	{
		return "Airport{" + "code='" + code + '\'' + ", name='" + name + '\'' + ", city='" + city + '\'' + ", country='" + country + '\'' + ", zoneId=" + zoneId + '}';
	}
}
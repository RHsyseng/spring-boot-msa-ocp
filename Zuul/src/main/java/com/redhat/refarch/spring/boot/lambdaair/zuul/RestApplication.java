package com.redhat.refarch.spring.boot.lambdaair.zuul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
@EnableZuulProxy
public class RestApplication
{
	private static Logger logger = Logger.getLogger( RestApplication.class.getName() );

	public static void main(String[] args)
	{
		List<Object> sources = new ArrayList<>();
		sources.add( RestApplication.class );
		try
		{
			for( Resource resource : new PathMatchingResourcePatternResolver().getResources( "file:/groovy/*.groovy" ) )
			{
				logger.info( "Found and will load groovy script " + resource.getFilename() );
				sources.add( resource );
			}
			if( sources.size() == 1 )
			{
				logger.info( "No groovy script found under /groovy/*.groovy" );
			}
		}
		catch( IOException e )
		{
			logger.log( Level.WARNING, "Failed to query classpath for groovy scripts", e );
		}
		SpringApplication.run( sources.toArray(), args );
	}
}
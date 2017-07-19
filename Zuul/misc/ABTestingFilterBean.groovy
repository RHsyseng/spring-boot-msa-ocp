package groovy

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import com.netflix.zuul.util.HTTPRequestUtils

import java.util.logging.Logger

class ABTestingFilterBean extends ZuulFilter {

    private static Logger logger = Logger.getLogger( ABTestingFilterBean.class.getName() );

    @Override
    String filterType() {
        return "pre"
    }

    @Override
    int filterOrder() {
        return 99
    }

    @Override
    boolean shouldFilter() {
        if( !RequestContext.currentContext.getRequest().getRequestURI().matches("/sales.*") )
        {
            //Won't filter this request URL
            false
        }
        else
        {
            String caller = new HTTPRequestUtils().getHeaderValue("baggage-forwarded-for");
            logger.info("Caller IP address is " + caller)
            int lastDigit = caller.reverse().take(1) as Integer
            if( lastDigit % 2 == 0 )
            {
                //Even IP address won't be filtered
                false
            }
            else
            {
                //Odd IP address will be filtered
                true
            }
        }
    }

    @Override
    Object run() {
        println("Running filter")
        RequestContext.currentContext.routeHost = new URL("http://salesv2:8080")
    }
}

beans {
    abTestingFilterBean(ABTestingFilterBean) {
        new groovy.ABTestingFilterBean();
    }
}